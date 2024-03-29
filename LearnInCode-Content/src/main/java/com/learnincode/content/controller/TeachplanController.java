package com.learnincode.content.controller;

import com.learnincode.content.model.dto.BindTeachplanMediaDto;
import com.learnincode.content.model.dto.SaveTeachplanDto;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.model.po.Teachplan;
import com.learnincode.content.service.TeachplanMediaService;
import com.learnincode.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
@RequestMapping("/teachplan")
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private TeachplanMediaService teachplanMediaService;

    /**
     *  根据课程id查询对应课程计划树
     * @param courseId 课程id
     * @return
     */
    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachplanDto> getTeachPlanTree(@PathVariable Long courseId)
    {
        return teachplanService.getTeachPlanTree(courseId);
    }



    /**
     * @author CalmKin
     * @description 前端会传入parentid、grade（层级）、courseid（所属课程id）
     * @version 1.0
     * @date 2024/1/19 20:39
     */
    @ApiOperation("新增或修改课程计划")
    @PostMapping
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplanDto){
        teachplanService.saveTeachplan(teachplanDto);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("{teachplanId}")
    public void deleteTeachplan(@PathVariable Long teachplanId)
    {
        teachplanService.deleteTeachplan(teachplanId);
    }

    @ApiOperation("下移课程计划")
    @PostMapping("/movedown/{id}")
    public void movedownTeachplan (@PathVariable Long id)
    {
        teachplanService.movedownTeachplan(id);
    }
    @ApiOperation("上移课程计划")
    @PostMapping("/moveup/{id}")
    public void moveupTeachplan (@PathVariable Long id)
    {
        teachplanService.moveupTeachplan(id);
    }


    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.attachMedia(bindTeachplanMediaDto);
    }

    @ApiOperation(value = "解除课程计划和视频资源的绑定")
    @DeleteMapping("/association/media/{teachPlanId}/{mediaId}")
    public void detachMedia(@PathVariable Long teachPlanId, @PathVariable Long mediaId)
    {
        teachplanMediaService.detachMedia(teachPlanId,mediaId);
    }
    @ApiOperation(value = "根据id查询课程计划")
    @GetMapping("/open/{teachPlanId}")
    public Teachplan getTeachPlanById(@PathVariable Long teachPlanId)
    {
        return teachplanService.getById(teachPlanId);
    }



}
