package com.learnincode.learning.service;

import com.learnincode.learning.model.dto.ChooseCourseDto;
import org.springframework.stereotype.Service;


/**
 * @author CalmKin
 * @description 我的课程表接口
 * @version 1.0
 * @date 2024/2/3 17:41
 */
@Service
public interface MyCourseTablesService {


    ChooseCourseDto addChooseCourse(String userId, Long courseId);
}
