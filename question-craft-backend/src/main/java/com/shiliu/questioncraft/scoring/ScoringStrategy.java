package com.shiliu.questioncraft.scoring;

import com.shiliu.questioncraft.model.entity.App;
import com.shiliu.questioncraft.model.entity.UserAnswer;

import java.util.List;

/**
 * 评分策略
 *
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
public interface ScoringStrategy {

    /**
     * 评分
     * @param choices 用户答案
     * @param app 应用
     * @return 题目答案（得分结果）
     * @throws Exception
     */
    UserAnswer doScore(List<String> choices, App app) throws Exception;;
}
