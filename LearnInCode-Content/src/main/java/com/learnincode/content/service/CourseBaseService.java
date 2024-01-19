package com.learnincode.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.content.model.dto.AddCourseDto;
import com.learnincode.content.model.dto.CourseBaseInfoDto;
import com.learnincode.content.model.dto.QueryCourseParamsDto;
import com.learnincode.content.model.dto.UpdateCourseDto;
import com.learnincode.content.model.po.CourseBase;

/**
* @author 86158
* @description 针对表【course_base(课程基本信息)】的数据库操作Service
* @createDate 2024-01-17 11:30:41
*/
public interface CourseBaseService extends IService<CourseBase> {

    PageResult<CourseBase> pageCourseBase(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    CourseBaseInfoDto addCourseBaseInfo(Long companyId , AddCourseDto addCourseDto);

    CourseBaseInfoDto getCourseBaseInfoDtoById(Long courseId);

    CourseBaseInfoDto updateCourseBaseInfo(Long companyId,UpdateCourseDto dto);
}
