package com.learnincode.content.controller;

import com.learnincode.content.model.dto.AddCourseTeacherDto;
import com.learnincode.content.model.po.CourseTeacher;
import com.learnincode.content.service.CourseTeacherService;
import com.learnincode.content.utils.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courseTeacher")
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("根据课程id查询对应教师列表")
    @GetMapping("/list/{id}")
    public List<CourseTeacher> listTeacherByCourseId(@PathVariable Long id)
    {
        return courseTeacherService.listTeacherByCourseId(id);
    }

    @ApiOperation("添加教师信息")
    @PostMapping
    public CourseTeacher addCourseTeacher(@RequestBody AddCourseTeacherDto dto)
    {

                SecurityUtil.User user = SecurityUtil.getUser();
        Long companyId = Long.valueOf(user.getCompanyId());

        CourseTeacher courseTeacher = courseTeacherService.addCourseTeacher(companyId,dto);
        return courseTeacher;
    }

    @ApiOperation("修改教师信息")
    @PutMapping
    public CourseTeacher updateCourseTeacher(@RequestBody CourseTeacher dto)
    {
        courseTeacherService.updateById(dto);
        return dto;
    }

    @ApiOperation("删除某个课程的某个教师")
    @DeleteMapping("/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId)
    {
                SecurityUtil.User user = SecurityUtil.getUser();
        Long companyId = Long.valueOf(user.getCompanyId());
        courseTeacherService.deleteCourseTeacher(companyId,courseId, teacherId);
    }

}
