package com.learnincode.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.content.mapper.CourseBaseMapper;
import com.learnincode.content.mapper.CourseTeacherMapper;
import com.learnincode.content.model.dto.AddCourseTeacherDto;
import com.learnincode.content.model.po.CourseBase;
import com.learnincode.content.model.po.CourseTeacher;
import com.learnincode.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author 86158
* @description 针对表【course_teacher(课程-教师关系表)】的数据库操作Service实现
* @createDate 2024-01-20 10:24:57
*/
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher>
    implements CourseTeacherService{

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacher> listTeacherByCourseId(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CourseTeacher::getCourseId, courseId);

        List<CourseTeacher> list = list(lqw);

        return list;
    }

    @Override
    @Transactional
    public CourseTeacher addCourseTeacher(Long companyId,AddCourseTeacherDto dto) {
        if(companyId == null) throw new BusinessException("不是该课程所属机构,无法操作");

        Long courseId = dto.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(!companyId.equals(courseBase.getCompanyId())) throw new BusinessException("不是该课程所属机构,无法操作");

        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtils.copyProperties(dto, courseTeacher);
        save(courseTeacher);
        return courseTeacher;
    }

    @Override
    public void deleteCourseTeacher(Long companyId,Long courseId, Long teacherId) {
        if(companyId == null) throw new BusinessException("不是该课程所属机构,无法操作");
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(!companyId.equals(courseBase.getCompanyId())) throw new BusinessException("不是该课程所属机构,无法操作");

        LambdaQueryWrapper<CourseTeacher> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, teacherId);
        remove(lqw);
    }



}




