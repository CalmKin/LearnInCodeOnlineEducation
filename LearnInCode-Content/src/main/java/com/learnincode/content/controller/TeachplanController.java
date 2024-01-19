package com.learnincode.content.controller;

import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
@RequestMapping("/teachplan")
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    /**
     *  根据课程id查询对应课程计划树
     * @param courseId
     * @return
     */
    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachplanDto> getTeachPlanTree(@PathVariable Long courseId)
    {
        return teachplanService.getTeachPlanTree(courseId);
    }


}
