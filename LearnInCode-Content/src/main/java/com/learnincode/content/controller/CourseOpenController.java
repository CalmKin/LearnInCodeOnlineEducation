package com.learnincode.content.controller;


import com.learnincode.base.model.RestResponse;
import com.learnincode.content.feignclient.MediaFeignClient;
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

    @Autowired
    private MediaFeignClient mediaFeignClient;

    /**
     * 根据课程id获取未发布课程详细信息
     * @param courseId
     * @return
     */
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {
        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        return coursePreviewInfo;
    }

    @GetMapping("/content/course/whole/{courseId}")
    public CoursePreviewDto getPublishedCourseInfo(@PathVariable("courseId") Long courseId)
    {
        return getPreviewInfo(courseId);
    }


    /**
     * @author CalmKin
     * @description 根据视频id返回视频播放地址(需要在课程发布表里面查)
     * @version 1.0
     * @date 2024/2/20 16:57
     */
    @GetMapping("media/preview/{mediaId}")
    public RestResponse getMediaUrl(@PathVariable String mediaId)
    {
        // 根据媒资服务的FeignClient查询媒资信息
         return mediaFeignClient.getPlayUrlByMediaId(mediaId);
    }


}