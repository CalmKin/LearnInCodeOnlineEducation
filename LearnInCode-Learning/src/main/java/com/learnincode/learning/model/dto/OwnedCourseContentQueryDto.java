package com.learnincode.learning.model.dto;

import com.learnincode.learning.model.po.OwnedCourse;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @description 我的课程查询条件
 */
@Data
@ToString
public class OwnedCourseContentQueryDto extends OwnedCourse {

    /**
     * 最近学习时间
     */
    private LocalDateTime learnDate;

    /**
     * 学习时长
     */
    private Long learnLength;

    /**
     * 章节id
     */
    private Long teachplanId;

    /**
     * 章节名称
     */
    private String teachplanName;


}
