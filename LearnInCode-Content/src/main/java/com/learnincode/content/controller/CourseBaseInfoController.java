package com.learnincode.content.controller;


import com.learnincode.base.exception.ValidationGroups;
import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.content.model.dto.AddCourseDto;
import com.learnincode.content.model.dto.CourseBaseInfoDto;
import com.learnincode.content.model.dto.QueryCourseParamsDto;
import com.learnincode.content.model.dto.UpdateCourseDto;
import com.learnincode.content.model.po.CourseBase;
import com.learnincode.content.service.CourseBaseService;
import com.learnincode.content.utils.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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

    /**
     * @param addCourseDto 前端提交课程信息表单
     * @return 课程完整信息
     */
    @ApiOperation("新增课程信息接口")
    @PostMapping
    public CourseBaseInfoDto addCourseBaseInfo(@RequestBody @Validated(ValidationGroups.Inster.class) AddCourseDto addCourseDto)
    {
        SecurityUtil.User user = SecurityUtil.getUser();
        Long companyId = Long.valueOf(user.getCompanyId());
        return courseBaseService.addCourseBaseInfo(companyId,addCourseDto);
    }

    @ApiOperation("根据课程id查询课程信息")
    @GetMapping("/{courseId}")
    public CourseBaseInfoDto getCourseBaseInfoById(@PathVariable Long courseId)
    {
        return courseBaseService.getCourseBaseInfoDtoById(courseId);
    }


    @ApiOperation("修改课程信息")
    @PutMapping
    public CourseBaseInfoDto updateCourseBaseInfo(@RequestBody @Validated UpdateCourseDto dto)
    {
                SecurityUtil.User user = SecurityUtil.getUser();
        Long companyId = Long.valueOf(user.getCompanyId());
        return courseBaseService.updateCourseBaseInfo(companyId, dto);
    }

    
    /**
     * @author CalmKin
     * @description
     * @version 1.0
     * @date 2024/1/20 10:57
     */
    @ApiOperation("删除课程信息")
    @DeleteMapping("{id}")
    public void deleteCourseBase(@PathVariable Long id)
    {
        courseBaseService.deleteCourseBase(id);
    }

}
