package com.learnincode.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.content.mapper.TeachplanMapper;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.model.po.Teachplan;
import com.learnincode.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 86158
* @description 针对表【teachplan(课程计划)】的数据库操作Service实现
* @createDate 2024-01-19 19:02:18
*/
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan>
    implements TeachplanService{

    @Autowired
    private TeachplanMapper teachplanMapper;


    @Override
    public List<TeachplanDto> getTeachPlanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        System.out.println(teachplanDtos);
        return teachplanDtos;
    }
}




