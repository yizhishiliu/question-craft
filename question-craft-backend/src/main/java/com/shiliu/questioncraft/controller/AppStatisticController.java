package com.shiliu.questioncraft.controller;

import com.shiliu.questioncraft.common.BaseResponse;
import com.shiliu.questioncraft.common.ErrorCode;
import com.shiliu.questioncraft.common.ResultUtils;
import com.shiliu.questioncraft.exception.ThrowUtils;
import com.shiliu.questioncraft.mapper.UserAnswerMapper;
import com.shiliu.questioncraft.model.dto.statistic.AppAnswerCountDTO;
import com.shiliu.questioncraft.model.dto.statistic.AppAnswerResultCountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * App 统计分析控制器
 *
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 */
@RestController
@RequestMapping("/app/statistic")
@Slf4j
public class AppStatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    /**
     * 应用答题数量统计(热门应用TOP10)
     *
     * @return 答题数量统计
     */
    @GetMapping("/answer_count")
    public BaseResponse<List<AppAnswerCountDTO>> getAnswerCount() {
        return ResultUtils.success(userAnswerMapper.doAppAnswerCount());
    }

    /**
     * 应用答题结果统计
     *
     * @param appId 应用id
     * @return 答题结果统计
     */
    @GetMapping("/answer_result_count")
    public BaseResponse<List<AppAnswerResultCountDTO>> getAnswerResultCount(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userAnswerMapper.doAppAnswerResultCount(appId));
    }
}
