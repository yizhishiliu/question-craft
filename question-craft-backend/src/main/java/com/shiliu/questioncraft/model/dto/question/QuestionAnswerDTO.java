package com.shiliu.questioncraft.model.dto.question;

import lombok.Data;

/**
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 * 题目答案封装类（用于AI评分）
 */
@Data
public class QuestionAnswerDTO {

    /**
     * 题目
     */
    private String title;

    /**
     * 用户答案
     */
    private String userAnswer;
}
