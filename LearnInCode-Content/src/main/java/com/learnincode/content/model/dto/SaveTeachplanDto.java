package com.learnincode.content.model.dto;


import lombok.Data;

/**
 * @author CalmKin
 * @description 课程计划dto，可以同时用于新增、修改，根据是否带id区分操作
 * @version 1.0
 * @date 2024/1/19 19:56
 */
@Data
public class SaveTeachplanDto {

    /***
     * 教学计划id
     */
    private Long id;

    /**
     * 课程计划名称
     */
    private String pname;

    /**
     * 课程计划父级Id
     */
    private Long parentid;

    /**
     * 层级，分为1、2、3级
     */
    private Integer grade;

    /**
     * 课程类型:1视频、2文档
     */
    private String mediaType;


    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 课程发布标识
     */
    private Long coursePubId;


    /**
     * 是否支持试学或预览（试看）
     */
    private String isPreview;


}
