package com.learnincode.content.model.dto;

import lombok.Data;
import lombok.ToString;


 /**
  * @description 课程查询参数Dto
  * @author CalmKin
  * @version 1.0
  * @date 2024/1/12 20:28
  */
@Data
@ToString
public class QueryCourseParamsDto {

    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;

}