package com.learnincode.content.model.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * @author CalmKin
 * @description 更新课程dto，因为需要回显数据，所以就比新增多了个id
 * @version 1.0
 * @date 2024/1/19 10:40
 */
@Data
@ApiModel(description="修改课程基本信息")
public class UpdateCourseDto extends AddCourseDto{
    @Min(0L)
    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}