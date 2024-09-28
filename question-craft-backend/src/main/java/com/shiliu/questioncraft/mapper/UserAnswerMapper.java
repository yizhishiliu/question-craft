package com.shiliu.questioncraft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiliu.questioncraft.model.dto.statistic.AppAnswerCountDTO;
import com.shiliu.questioncraft.model.dto.statistic.AppAnswerResultCountDTO;
import com.shiliu.questioncraft.model.entity.UserAnswer;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ada
 * @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
 * @createDate 2024-08-18 12:25:34
 * @Entity com.shiliu.questioncraft.model.entity.UserAnswer
 */
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    /**
     * 应用答题数量统计
     *
     * @return 用户答题统计列表
     */
    List<AppAnswerCountDTO> doAppAnswerCount();

    /**
     * 应用答题结果统计
     *
     * @return 用户答题结果统计列表
     */
    List<AppAnswerResultCountDTO> doAppAnswerResultCount(Long appId);
}




