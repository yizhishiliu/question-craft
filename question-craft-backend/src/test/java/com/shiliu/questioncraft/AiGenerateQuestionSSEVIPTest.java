package com.shiliu.questioncraft;

import com.shiliu.questioncraft.controller.QuestionController;
import com.shiliu.questioncraft.model.dto.question.AiGenerateQuestionRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
@SpringBootTest
public class AiGenerateQuestionSSEVIPTest {

    @Resource
    private QuestionController questionController;

    @Test
    void aiGenerateQuestionSSEVIPTest() throws InterruptedException {
        AiGenerateQuestionRequest request = new AiGenerateQuestionRequest();
        request.setAppId(3L);
        request.setQuestionNumber(10);
        request.setOptionNumber(2);

        questionController.aiGenerateQuestionSSE(request, false);
        questionController.aiGenerateQuestionSSE(request, false);
        questionController.aiGenerateQuestionSSE(request, true);

        Thread.sleep(1000000L);
    }
}
