package com.learnincode.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.model.po.Teachplan;

import java.util.List;

/**
* @author 86158
* @description 针对表【teachplan(课程计划)】的数据库操作Mapper
* @createDate 2024-01-19 19:02:18
* @Entity generator.domain.Teachplan
*/
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    public List<TeachplanDto> selectTreeNodes(Long courseId);
}




