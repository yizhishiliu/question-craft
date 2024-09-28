package com.shiliu.questioncraft.model.dto.statistic;

import lombok.Data;

import java.io.Serializable;

/**
 * App 用户提交答案数统计
 *
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
@Data
public class AppAnswerCountDTO implements Serializable {

    private Long appId;

    /**
     * 用户提交答案数
     */
    private Long answerCount;

    private static final long serialVersionUID = 1L;
}
