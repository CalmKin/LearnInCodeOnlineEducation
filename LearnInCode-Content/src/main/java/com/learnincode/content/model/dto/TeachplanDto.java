package com.learnincode.content.model.dto;

import com.learnincode.content.model.po.Teachplan;
import com.learnincode.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;



/**
 * @author CalmKin
 * @description 课程计划查询DTO，一个课程计划会有若干个子章节，每个章节都可能有关联的媒资
 * @version 1.0
 * @date 2024/1/19 11:50
 */
@Data
public class TeachplanDto extends Teachplan {

    // 课程计划子章节
    private List<TeachplanDto> teachPlanTreeNodes;

    // 章节关联媒资信息
    private TeachplanMedia teachplanMedia;

}
