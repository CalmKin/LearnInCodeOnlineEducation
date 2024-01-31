package com.learnincode.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;


/**
 * @author CalmKin
 * @description 课程预览数据模型
 * @version 1.0
 * @date 2024/1/31 14:17
 */
@Data
@ToString
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachplanDto> teachplans;

    // todo 师资信息

}