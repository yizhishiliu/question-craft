package com.shiliu.questioncraft.model.dto.statistic;

import lombok.Data;

import java.io.Serializable;

/**
 * App 用户答题结果数统计
 *
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
@Data
public class AppAnswerResultCountDTO implements Serializable {

    private String resultName;

    /**
     * 用户答题结果数
     */
    private Long resultCount;

    private static final long serialVersionUID = 1L;
}
