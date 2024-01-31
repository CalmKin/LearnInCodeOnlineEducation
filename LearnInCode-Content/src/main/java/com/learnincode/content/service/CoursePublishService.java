package com.learnincode.content.service;


import com.learnincode.content.model.dto.CoursePreviewDto;


/**
 * @author CalmKin
 * @description 课程预览、发布接口
 * @version 1.0
 * @date 2024/1/31 14:26
 */
public interface CoursePublishService  {
    /**
     * 根据课程id获取课程预览信息
     * @param courseId
     * @return
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);
}
