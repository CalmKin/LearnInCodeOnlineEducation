package com.learnincode.search.controller;

import com.learnincode.base.exception.BusinessException;
import com.learnincode.search.po.CourseIndex;
import com.learnincode.search.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 课程索引接口
 */
@Api(value = "课程信息索引接口", tags = "课程信息索引接口")
@RestController
@RequestMapping("/index")
public class CourseIndexController {

    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Autowired
    IndexService indexService;

    @ApiOperation("添加课程索引")
    @PostMapping("course")
    public Boolean add(@RequestBody CourseIndex courseIndex) {

        Long id = courseIndex.getId();
        if(id==null){
            throw new BusinessException("课程id为空");
        }
        Boolean result = indexService.addCourseIndex(courseIndexStore, String.valueOf(id), courseIndex);
        if(!result){
            throw new BusinessException("添加课程索引失败");
        }
        return result;

    }
}
