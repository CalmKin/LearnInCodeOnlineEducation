package com.learnincode.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learnincode.content.model.dto.SaveTeachplanDto;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.model.po.Teachplan;

import java.util.List;

/**
* @author 86158
* @description 针对表【teachplan(课程计划)】的数据库操作Service
* @createDate 2024-01-19 19:02:18
*/
public interface TeachplanService extends IService<Teachplan> {

    List<TeachplanDto> getTeachPlanTree(Long courseId);

    void saveTeachplan(SaveTeachplanDto teachplanDto);
}
