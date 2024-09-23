package com.shiliu.questioncraft.scoring;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiliu.questioncraft.common.ErrorCode;
import com.shiliu.questioncraft.exception.BusinessException;
import com.shiliu.questioncraft.manager.AiManager;
import com.shiliu.questioncraft.model.dto.question.QuestionAnswerDTO;
import com.shiliu.questioncraft.model.dto.question.QuestionContentDTO;
import com.shiliu.questioncraft.model.entity.App;
import com.shiliu.questioncraft.model.entity.Question;
import com.shiliu.questioncraft.model.entity.UserAnswer;
import com.shiliu.questioncraft.model.vo.QuestionVO;
import com.shiliu.questioncraft.service.QuestionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI 测评类应用评分策略
 *
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AiTestScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedissonClient redissionClient;

    /**
     * 分布式锁 key
     */
    private static final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";

    /**
     * AI 评分结果本地缓存
     */
    private final Cache<String, String> answerCacheMap =
            Caffeine.newBuilder()
                    .initialCapacity(1024)
                    // 过期时间 5 分钟
                    .expireAfterAccess(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * AI 评分系统消息
     */
    private static final String AI_TEST_SCORING_SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
            "1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 200 字）\n" +
            "2. 严格按照下面的 json 格式输出评价名称和评价描述\n" +
            "```\n" +
            "{\"resultName\": \"评价名称\", \"resultDesc\": \"评价描述\"}\n" +
            "```\n" +
            "3. 返回格式必须为 JSON 对象";

    /**
     * AI 评分用户消息
     *
     * @param app                    应用
     * @param questionContentDTOList 题目列表
     * @param choices                用户选择的答案
     * @return 结果
     */
    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Long appId = app.getId();
        String jsonStr = JSONUtil.toJsonStr(choices);

        // 构建缓存key
        String cacheKey = buildCacheKey(appId, jsonStr);
        // 查找缓存
        String cacheAnswer = answerCacheMap.getIfPresent(cacheKey);
        // 如果缓存中有答案，直接返回
        if (StrUtil.isNotBlank(cacheAnswer)) {
            // 构造返回值，填充答案对应的属性，返回评分结果
            UserAnswer userAnswer = JSONUtil.toBean(cacheAnswer, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            return userAnswer;
        }

        // 定义锁
        RLock lock = redissionClient.getLock(AI_ANSWER_LOCK + cacheKey);

        try {
            // 竞争锁
            boolean res = lock.tryLock(3, 15, TimeUnit.SECONDS);

            // 持有锁则正常执行业务，如果没有获取到锁，强行返回
            if (!res) {
                return null;
            }

            // 1. 根据 id 获取题目
            Question question = questionService.getOne(
                    Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
            );

            QuestionVO questionVO = QuestionVO.objToVo(question);
            List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

            // 2. 调用 AI 获取结果
            // 封装prompt
            String userMessage = getAiTestScoringUserMessage(app, questionContent, choices);

            // 调用 AI
            String result = aiManager.doSyncRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, userMessage, null);

            // 解析结果
            int startIndex = result.indexOf("{");
            int endIndex = result.lastIndexOf("}") + 1;
            String jsonResult = result.substring(startIndex, endIndex);

            // 缓存结果
            answerCacheMap.put(cacheKey, jsonResult);

            // 3. 构造返回值，填充答案对应的属性，返回评分结果
            UserAnswer userAnswer = JSONUtil.toBean(jsonResult, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            return userAnswer;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI评分失败");
        } finally {
            // 释放锁
            if (lock != null && lock.isLocked()) {
                // 当前线程释放锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * 构建缓存key
     * @param appId 应用id
     * @param choices 用户选择的答案
     * @return 缓存key
     */
    private String buildCacheKey(Long appId, String choices) {
        return DigestUtil.md5Hex(appId + ":" + choices);
    }
}
