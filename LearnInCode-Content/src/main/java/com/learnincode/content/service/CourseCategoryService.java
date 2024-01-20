package com.learnincode.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learnincode.content.model.dto.CourseCategoryTreeDto;
import com.learnincode.content.model.po.CourseCategory;

import java.util.List;

/**
* @author 86158
* @description 针对表【course_category(课程分类)】的数据库操作Service
* @createDate 2024-01-18 09:30:29
*/
public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryTreeDto> queryTreeNodes(String number);
}
