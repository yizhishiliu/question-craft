package com.shiliu.questioncraft.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 * AI 生成题目请求
 */
@Data
public class AiGenerateQuestionRequest implements Serializable {

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 题目数量
     */
    int questionNumber = 10;

    /**
     * 选项数量
     */
    int optionNumber = 4;

    private static final long serialVersionUID = 1L;
}
