package com.learnincode.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learnincode.content.model.dto.CourseCategoryTreeDto;
import com.learnincode.content.model.po.CourseCategory;

import java.util.List;

/**
* @author 86158
* @description 针对表【course_category(课程分类)】的数据库操作Mapper
* @createDate 2024-01-18 09:30:29
* @Entity generator.domain.CourseCategory
*/
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
    List<CourseCategoryTreeDto> selectTreeNodes(String id);
}




