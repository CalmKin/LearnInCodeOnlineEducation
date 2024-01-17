package com.learnincode.content.controller;


import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.content.model.dto.QueryCourseParamsDto;
import com.learnincode.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;


/**
 * @description 课程信息相关接口
 * @author CalmKin
 * @version 1.0
 * @date 2024/1/12 20:52
 */
@RestController
@RequestMapping("/course")
public class CourseBaseInfoController {
    /**
     * @description 课程信息分页查询
     * @author CalmKin
     * @version 1.0
     * @date 2024/1/12 20:52
     */
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto)
    {
        CourseBase courseBase = new CourseBase();
        List<CourseBase> courseBases = Collections.singletonList(courseBase);
        PageResult<CourseBase> result= new PageResult<>(courseBases,1L,1L,3L);
        return result;
    }


}
