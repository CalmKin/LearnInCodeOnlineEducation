package com.learnincode.content.api;


import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.content.model.dto.QueryCourseParamsDto;
import com.learnincode.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




/**
 * @description 课程信息相关接口
 * @author CalmKin
 * @version 1.0
 * @date 2024/1/12 20:52
 */
@RestController
public class CourseBaseInfoController {


    /**
     * @description 课程信息分页查询
     * @author CalmKin
     * @version 1.0
     * @date 2024/1/12 20:52
     */
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto)
    {

        return null;
    }


}
