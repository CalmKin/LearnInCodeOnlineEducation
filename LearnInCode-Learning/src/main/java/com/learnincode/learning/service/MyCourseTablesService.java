package com.learnincode.learning.service;

import com.learnincode.learning.model.dto.ChoosedCourseDto;
import com.learnincode.learning.model.dto.OwnedCourseStatusDto;
import org.springframework.stereotype.Service;


/**
 * @author CalmKin
 * @version 1.0
 * @description 我的课程表接口
 * @date 2024/2/3 17:41
 */
@Service
public interface MyCourseTablesService {


    ChoosedCourseDto addChooseCourse(String userId, Long courseId);

    OwnedCourseStatusDto getLearningStatus(String userId, Long courseId);

}
