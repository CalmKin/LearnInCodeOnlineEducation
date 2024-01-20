package com.learnincode.content.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddCourseTeacherDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 教师标识
     */
    private String teacherName;

    /**
     * 教师职位
     */
    private String position;

    /**
     * 教师简介
     */
    private String introduction;


}
