package com.learnincode.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learnincode.content.model.dto.AddCourseTeacherDto;
import com.learnincode.content.model.po.CourseTeacher;

import java.util.List;

/**
* @author 86158
* @description 针对表【course_teacher(课程-教师关系表)】的数据库操作Service
* @createDate 2024-01-20 10:24:57
*/
public interface CourseTeacherService extends IService<CourseTeacher> {

    List<CourseTeacher> listTeacherByCourseId(Long courseId);

    CourseTeacher addCourseTeacher(Long companyId,AddCourseTeacherDto dto);

    void deleteCourseTeacher(Long companyId,Long courseId, Long teacherId);
}
