package com.learnincode.learning.controller;

import com.learnincode.base.model.PageResult;
import com.learnincode.learning.model.dto.ChooseCourseDto;
import com.learnincode.learning.model.dto.CourseTablesDto;
import com.learnincode.learning.model.dto.MyCourseTableParams;
import com.learnincode.learning.model.po.CourseTables;
import com.learnincode.learning.service.MyCourseTablesService;
import com.learnincode.learning.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 我的课程表接口
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {

    @Autowired
    private MyCourseTablesService myCourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public ChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = user.getId();

        return myCourseTablesService.addChooseCourse(userId, courseId);
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public CourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {

        return null;

    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<CourseTables> mycoursetable(MyCourseTableParams params) {
        return null;
    }

}
