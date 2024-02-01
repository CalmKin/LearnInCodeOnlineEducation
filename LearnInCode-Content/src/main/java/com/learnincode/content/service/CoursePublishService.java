package com.learnincode.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.learnincode.content.model.dto.CoursePreviewDto;
import com.learnincode.content.model.po.CoursePublish;


/**
 * @author CalmKin
 * @description 课程预览、发布接口
 * @version 1.0
 * @date 2024/1/31 14:26
 */
public interface CoursePublishService extends IService<CoursePublish> {
     void commitAudit(Long companyId, Long courseId);

    /**
     * 根据课程id获取课程预览信息
     * @param courseId
     * @return
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    void coursepublish(Long companyId, Long courseId);
}
