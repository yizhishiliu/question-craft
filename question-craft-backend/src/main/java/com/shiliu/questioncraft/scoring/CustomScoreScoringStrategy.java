package com.shiliu.questioncraft.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shiliu.questioncraft.model.dto.question.QuestionContentDTO;
import com.shiliu.questioncraft.model.entity.App;
import com.shiliu.questioncraft.model.entity.Question;
import com.shiliu.questioncraft.model.entity.ScoringResult;
import com.shiliu.questioncraft.model.entity.UserAnswer;
import com.shiliu.questioncraft.model.vo.QuestionVO;
import com.shiliu.questioncraft.service.QuestionService;
import com.shiliu.questioncraft.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 自定义打分类应用评分策略
 *
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
public class CustomScoreScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        // 1. 根据 id 获取题目和结果信息（按分数降序排序）
        Long appId = app.getId();
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId)
                        .orderByDesc(ScoringResult::getResultScoreRange)
        );

        // 2. 统计用户得分
        int totalScore = 0;
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

        // 遍历题目列表
        for (QuestionContentDTO questionContentDTO : questionContent) {
            // 遍历答案列表
            for (String answer : choices) {
                // 遍历题目中的选项
                for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                    // 如果答案和选项的key匹配
                    if (option.getKey().equals(answer)) {
                        Integer score = Optional.of(option.getScore()).orElse(0);
                        totalScore += score;
                    }
                }
            }
        }

        // 3. 遍历得分结果，找到第一个用户得分大于等于结果分数范围的结果，作为最终结果
        // 初始化评分结果
        ScoringResult maxScoringResult = scoringResultList.get(0);
        for (ScoringResult scoringResult : scoringResultList) {
            if (totalScore >= scoringResult.getResultScoreRange()) {
                maxScoringResult = scoringResult;
                break;
            }
        }

        // 4. 构造返回值，填充答案对象属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;
    }
}
