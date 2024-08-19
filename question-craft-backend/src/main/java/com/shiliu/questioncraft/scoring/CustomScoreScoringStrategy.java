package com.shiliu.questioncraft.scoring;

import com.shiliu.questioncraft.model.entity.App;
import com.shiliu.questioncraft.model.entity.UserAnswer;

import java.util.List;

/**
 * 自定义打分类应用评分策略
 *
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
public class CustomScoreScoringStrategy implements ScoringStrategy {
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        return null;
    }
}
