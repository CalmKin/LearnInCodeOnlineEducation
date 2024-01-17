package com.learnincode.content.controller;


import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.content.model.dto.QueryCourseParamsDto;
import com.learnincode.content.model.po.CourseBase;
import com.learnincode.content.service.CourseBaseService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @description 课程信息相关接口
 * @author CalmKin
 * @version 1.0
 * @date 2024/1/12 20:52
 */
@RestController
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseService courseBaseService;

    /**
     * @description 课程信息分页查询
     * @author CalmKin
     * @version 1.0
     * @date 2024/1/12 20:52
     */
    @ApiOperation("课程分页查询接口")
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto)
    {
       return courseBaseService.pageCourseBase(pageParams,queryCourseParamsDto);
    }


}