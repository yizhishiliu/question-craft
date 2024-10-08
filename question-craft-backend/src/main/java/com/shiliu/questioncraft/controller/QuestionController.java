package com.shiliu.questioncraft.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiliu.questioncraft.annotation.AuthCheck;
import com.shiliu.questioncraft.common.BaseResponse;
import com.shiliu.questioncraft.common.DeleteRequest;
import com.shiliu.questioncraft.common.ErrorCode;
import com.shiliu.questioncraft.common.ResultUtils;
import com.shiliu.questioncraft.constant.UserConstant;
import com.shiliu.questioncraft.exception.BusinessException;
import com.shiliu.questioncraft.exception.ThrowUtils;
import com.shiliu.questioncraft.manager.AiManager;
import com.shiliu.questioncraft.model.dto.question.*;
import com.shiliu.questioncraft.model.entity.App;
import com.shiliu.questioncraft.model.entity.Question;
import com.shiliu.questioncraft.model.entity.User;
import com.shiliu.questioncraft.model.enums.AppTypeEnum;
import com.shiliu.questioncraft.model.vo.QuestionVO;
import com.shiliu.questioncraft.service.AppService;
import com.shiliu.questioncraft.service.QuestionService;
import com.shiliu.questioncraft.service.UserService;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 题目接口
 *
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Resource
    private AiManager aiManager;

    // 注入 VIP 线程池
    @Resource
    private Scheduler vipScheduler;

    // region 增删改查

    /**
     * 创建题目
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 在此处将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<QuestionContentDTO> questionContentDTO = questionAddRequest.getQuestionContent();
        question.setQuestionContent(JSONUtil.toJsonStr(questionContentDTO));
        // 数据校验
        questionService.validQuestion(question, true);
        // 填充默认值
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<QuestionContentDTO> questionContentDTO = questionUpdateRequest.getQuestionContent();
        question.setQuestionContent(JSONUtil.toJsonStr(questionContentDTO));
        // 数据校验
        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前登录用户创建的题目列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 编辑题目（给用户使用）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<QuestionContentDTO> questionContentDTO = questionEditRequest.getQuestionContent();
        question.setQuestionContent(JSONUtil.toJsonStr(questionContentDTO));
        // 数据校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionEditRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion

    // region AI生成题目
    private static final String GENERTE_QUESTION_SYSTEM_MESSAGE = "你是一位严谨的出题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "应用类别，\n" +
            "要生成的题目数，\n" +
            "每个题目的选项数\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来出题：\n" +
            "1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
            "2. 严格按照下面的 json 格式输出题目和选项\n" +
            "```\n" +
            "[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
            "```\n" +
            "title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
            "3. 检查题目是否包含序号，若包含序号则去除序号\n" +
            "4. 返回的题目列表格式必须为 JSON 数组";

    /**
     * 生成题目（给用户使用，用户消息）
     *
     * @param app
     * @param questionNumber
     * @param optionNumber
     * @return
     */
    private String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        userMessage.append(AppTypeEnum.getEnumByValue(app.getAppType()).getText() + "类").append("\n");
        userMessage.append(questionNumber).append("\n");
        userMessage.append(optionNumber);
        return userMessage.toString();
    }

    @PostMapping("/ai_generate")
    public BaseResponse<List<QuestionContentDTO>> aiGenerateQuestion(@RequestBody AiGenerateQuestionRequest aiGenerateQuestionRequest) {
        ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取参数
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();

        // 获取应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);

        // 封装prompt
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);

        // 调用 AI 生成题目
        String jsonResult = aiManager.doSyncStableRequest(GENERTE_QUESTION_SYSTEM_MESSAGE, userMessage);

        // 解析结果
        int startIndex = jsonResult.indexOf("[");
        int endIndex = jsonResult.lastIndexOf("]") + 1;
        String result = jsonResult.substring(startIndex, endIndex);
        List<QuestionContentDTO> questionContentDTOList = JSONUtil.toList(result, QuestionContentDTO.class);

        return ResultUtils.success(questionContentDTOList);
    }

    @GetMapping("/ai_generate/sse")
    public SseEmitter aiGenerateQuestionSSE(AiGenerateQuestionRequest aiGenerateQuestionRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取参数
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();

        // 获取应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);

        // 默认全局线程池
//        Scheduler scheduler = Schedulers.single();
        Scheduler scheduler = Schedulers.io();

        // 检测当前用户是否为 VIP 用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser != null && "vip".equals(loginUser.getUserRole())) {
            scheduler = vipScheduler;
        }

        // 封装prompt
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);

        // 建立 SSE 连接对象，0表示永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 调用 AI，流式返回
        Flowable<ModelData> dataFlowable = aiManager.doStreamRequest(GENERTE_QUESTION_SYSTEM_MESSAGE, userMessage, null);
        // 左括号计数器，除默认值外，当左括号数量等于右括号数量时，也就是值为0时，可以截取
        AtomicInteger leftBracketCounter = new AtomicInteger();
        // 拼接完整题目内容
        StringBuilder questionContent = new StringBuilder();
        // 订阅流
        dataFlowable
//                .observeOn(Schedulers.io())
                .observeOn(scheduler)
                // 获取智谱AI返回的内容
                .map(modelData -> modelData.getChoices().get(0).getDelta().getContent())
                // 去除所有空白字符（包括空格、制表符和换行符）
                .map(content -> content.replace("\\s", ""))
                // 过滤掉空字符
                .filter(StrUtil::isNotBlank)
                // 将content转换为字符数组，将其放入characterList中，最后返回一个Flowable（字符流）
                .flatMap(content -> {
                    List<Character> characterList = new ArrayList<>();
                    for (char c : content.toCharArray()) {
                        characterList.add(c);
                    }
                    return Flowable.fromIterable(characterList);
                })
                // 对每一个字符进行操作
                .doOnNext(c -> {
                    // 如果是左括号'{'，计数器加一
                    if (c == '{') {
                        leftBracketCounter.addAndGet(1);
                    }
                    if (leftBracketCounter.get() > 0) {
                        questionContent.append(c);
                    }
                    // 如果是右括号'}',计数器减一
                    if (c == '}') {
                        leftBracketCounter.addAndGet(-1);
                        // 如果计数器为0，发送questionContent的内容
                        if (leftBracketCounter.get() == 0) {
                            sseEmitter.send(JSONUtil.toJsonStr(questionContent.toString()));
                            // 重置题目内容
                            questionContent.setLength(0);
                        }
                    }
                })
                // 发生错误时打印错误日志
                .doOnError((e) -> log.error("AI 生成题目失败", e))
                // 完成时调用complete方法
                .doOnComplete(sseEmitter::complete)
                // 订阅
                .subscribe();
        return sseEmitter;
    }

    @GetMapping("/ai_generate/sse/test")
    public SseEmitter aiGenerateQuestionSSE(AiGenerateQuestionRequest aiGenerateQuestionRequest, boolean isVip) {
        ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取参数
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();

        // 获取应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);

        // 默认全局线程池
        Scheduler scheduler = Schedulers.single();

        // 检测当前用户是否为 VIP 用户
        if (isVip) {
            scheduler = vipScheduler;
        }

        // 封装prompt
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);

        // 建立 SSE 连接对象，0表示永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 调用 AI，流式返回
        Flowable<ModelData> dataFlowable = aiManager.doStreamRequest(GENERTE_QUESTION_SYSTEM_MESSAGE, userMessage, null);
        // 左括号计数器，除默认值外，当左括号数量等于右括号数量时，也就是值为0时，可以截取
        AtomicInteger leftBracketCounter = new AtomicInteger();
        // 拼接完整题目内容
        StringBuilder questionContent = new StringBuilder();
        // 订阅流
        dataFlowable
                .observeOn(scheduler)
                // 获取智谱AI返回的内容
                .map(modelData -> modelData.getChoices().get(0).getDelta().getContent())
                // 去除所有空白字符（包括空格、制表符和换行符）
                .map(content -> content.replace("\\s", ""))
                // 过滤掉空字符
                .filter(StrUtil::isNotBlank)
                // 将content转换为字符数组，将其放入characterList中，最后返回一个Flowable（字符流）
                .flatMap(content -> {
                    List<Character> characterList = new ArrayList<>();
                    for (char c : content.toCharArray()) {
                        characterList.add(c);
                    }
                    return Flowable.fromIterable(characterList);
                })
                // 对每一个字符进行操作
                .doOnNext(c -> {
                    // 如果是左括号'{'，计数器加一
                    if (c == '{') {
                        leftBracketCounter.addAndGet(1);
                    }
                    if (leftBracketCounter.get() > 0) {
                        questionContent.append(c);
                    }
                    // 如果是右括号'}',计数器减一
                    if (c == '}') {
                        leftBracketCounter.addAndGet(-1);
                        // 如果计数器为0，发送questionContent的内容
                        if (leftBracketCounter.get() == 0) {
                            // 输出当前线程名称
                            System.out.println(Thread.currentThread().getName());
                            // 模拟普通用户阻塞
                            if (!isVip) {
                                Thread.sleep(10000L);
                            }
                            sseEmitter.send(JSONUtil.toJsonStr(questionContent.toString()));
                            // 重置题目内容
                            questionContent.setLength(0);
                        }
                    }
                })
                // 发生错误时打印错误日志
                .doOnError((e) -> log.error("AI 生成题目失败", e))
                // 完成时调用complete方法
                .doOnComplete(sseEmitter::complete)
                // 订阅
                .subscribe();
        return sseEmitter;
    }
    // endregion
}
