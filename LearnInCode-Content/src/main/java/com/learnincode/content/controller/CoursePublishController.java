package com.learnincode.content.controller;


import com.alibaba.fastjson.JSON;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.content.model.dto.CourseBaseInfoDto;
import com.learnincode.content.model.dto.CoursePreviewDto;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.model.po.CoursePublish;
import com.learnincode.content.service.CoursePublishService;
import com.learnincode.content.utils.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

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

    @ApiOperation("获取已发布课程的所有信息(用于封装课程信息界面)")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId)
    {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        // 查不到
        if(coursePublish == null)
        {
            throw new BusinessException("课程信息不存在");
        }


        CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBase);

        String teachplanJson = coursePublish.getTeachplan();

        List<TeachplanDto> teachplans = JSON.parseArray(teachplanJson, TeachplanDto.class);

        coursePreviewDto.setCourseBase(courseBase);
        coursePreviewDto.setTeachplans(teachplans);

        return coursePreviewDto;
    }

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

    /**
     * 课程提交审核
     * @param courseId 课程id
     */
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
                SecurityUtil.User user = SecurityUtil.getUser();
        Long companyId = Long.valueOf(user.getCompanyId());
        coursePublishService.commitAudit(companyId,courseId);
    }

    /**
     *
     * @param courseId
     */
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
                SecurityUtil.User user = SecurityUtil.getUser();
        Long companyId = Long.valueOf(user.getCompanyId());
        coursePublishService.coursepublish(companyId, courseId);

    }


    /**
     * @author CalmKin
     * @description 对其他服务暴露的查询课程发布信息接口，r前缀标识为内部服务，不受权限控制
     * @version 1.0
     * @date 2024/2/3 16:26
     */
    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        return coursePublish;
    }


}