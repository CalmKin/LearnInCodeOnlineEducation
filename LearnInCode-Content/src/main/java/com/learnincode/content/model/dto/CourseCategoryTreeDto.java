package com.learnincode.content.model.dto;

import com.learnincode.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * @author CalmKin
 * @description 用来存放课程分类信息，subNode表示下一级分类
 * @version 1.0
 * @date 2024/1/17 20:10
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    // 存储子节点
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
