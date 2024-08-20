package com.shiliu.questioncraft.scoring;

import com.shiliu.questioncraft.common.ErrorCode;
import com.shiliu.questioncraft.exception.BusinessException;
import com.shiliu.questioncraft.model.entity.App;
import com.shiliu.questioncraft.model.entity.UserAnswer;
import com.shiliu.questioncraft.model.enums.AppScoringStrategyEnum;
import com.shiliu.questioncraft.model.enums.AppTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 编程式评分策略执行器
 *
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
@Service
@Deprecated
public class ScoringStrategyContext {

    @Resource
    private CustomScoreScoringStrategy customScoreScoringStrategy;

    @Resource
    private CustomTestScoringStrategy customTestScoringStrategy;

    /**
     * 评分
     *
     * @param choiceList 用户选择的答案列表
     * @param app 应用
     * @return
     * @throws Exception
     */
    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        AppTypeEnum appTypeEnum = AppTypeEnum.getEnumByValue(app.getAppType());
        AppScoringStrategyEnum appScoringStrategyEnum = AppScoringStrategyEnum.getEnumByValue(app.getScoringStrategy());
        if (appTypeEnum == null || appScoringStrategyEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
        }
        // 根据不同的应用类别和评分策略，选择对应的策略执行
        switch (appTypeEnum) {
            case SCORE:
                switch (appScoringStrategyEnum) {
                    case CUSTOM:
                        return customScoreScoringStrategy.doScore(choiceList, app);
                    case AI:
                        break;
                }
                break;
            case TEST:
                switch (appScoringStrategyEnum) {
                    case CUSTOM:
                        return customTestScoringStrategy.doScore(choiceList, app);
                    case AI:
                        break;
                }
                break;
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
    }
}
