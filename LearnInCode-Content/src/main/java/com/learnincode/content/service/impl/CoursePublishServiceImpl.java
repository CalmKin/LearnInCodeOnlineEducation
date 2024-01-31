package com.learnincode.content.service.impl;

import com.learnincode.content.model.dto.CourseBaseInfoDto;
import com.learnincode.content.model.dto.CoursePreviewDto;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.service.CourseBaseService;
import com.learnincode.content.service.CoursePublishService;
import com.learnincode.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private TeachplanService teachplanService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto viewObject = new CoursePreviewDto();
        // 查询课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfoDtoById(courseId);
        viewObject.setCourseBase(courseBaseInfo);

        // 查询课程计划
        List<TeachplanDto> teachPlans = teachplanService.getTeachPlanTree(courseId);
        viewObject.setTeachplans(teachPlans);

        return viewObject;
    }
}
