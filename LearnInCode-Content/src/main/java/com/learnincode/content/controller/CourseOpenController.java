package com.learnincode.content.controller;


import com.learnincode.content.model.dto.CoursePreviewDto;
import com.learnincode.content.service.CourseBaseService;
import com.learnincode.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author CalmKin
 * @description 无需登录也能访问的课程相关接口
 * @version 1.0
 * @date 2024/1/31 15:54
 */
@Api(value = "课程公开查询接口",tags = "课程公开查询接口")
@RestController
@RequestMapping("/open")
public class CourseOpenController {

    @Autowired
    private CourseBaseService courseBaseInfoService;

    @Autowired
    private CoursePublishService coursePublishService;

    /**
     * 根据课程id获取课程详细信息
     * @param courseId
     * @return
     */
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {
        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        return coursePreviewInfo;
    }

}