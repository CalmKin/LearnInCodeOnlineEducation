package com.learnincode.content.controller;


import com.learnincode.content.model.dto.CoursePreviewDto;
import com.learnincode.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author CalmKin
 * @description 课程预览，发布接口
 * @version 1.0
 * @date 2024/1/31 14:10
 */
@Controller     //注意这里不是restController，因为返回的是页面而不是json
public class CoursePublishController {


    @Autowired
    private CoursePublishService coursePublishService;

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){

        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        //设置模型数据
        modelAndView.addObject("name","小明");
        //设置模板名称
        modelAndView.setViewName("test");
        return modelAndView;
    }


}