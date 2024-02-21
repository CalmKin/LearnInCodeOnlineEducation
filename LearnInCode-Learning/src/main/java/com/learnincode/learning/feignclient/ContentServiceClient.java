package com.learnincode.learning.feignclient;

import com.learnincode.learning.feignclient.fallback.ContentServiceClientFallbackFactory;
import com.learnincode.learning.feignclient.model.Teachplan;
import com.learnincode.learning.model.po.CoursePublish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description 内容管理远程接口
 */
@FeignClient(value = "content-api", fallbackFactory = ContentServiceClientFallbackFactory.class)
public interface ContentServiceClient {


    /**
     * @author CalmKin
     * @description 通过feign查询已发布的课程信息
     * @version 1.0
     * @date 2024/2/21 9:50
     */
    @ResponseBody
    @GetMapping("/content/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId);

    @GetMapping("/teachplan/open/{teachPlanId}")
    public Teachplan getTeachPlanById(@PathVariable Long teachPlanId);


}
