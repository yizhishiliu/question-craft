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
     * @param choices
     * @param app
     * @return
     * @throws Exception
     */
    UserAnswer doScore(List<String> choices, App app) throws Exception;;
}
