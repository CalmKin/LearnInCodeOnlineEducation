package com.learnincode.search.service;


import com.learnincode.base.model.PageParams;
import com.learnincode.search.dto.SearchCourseParamDto;
import com.learnincode.search.dto.SearchPageResultDto;
import com.learnincode.search.po.CourseIndex;

/**
 * @description 课程搜索service
 */
public interface CourseSearchService {


    /**
     * @description 搜索课程列表
     * @param pageParams 分页参数
     * @param searchCourseParamDto 搜索条件
     * @return com.learnincode.base.model.PageResult<com.learnincode.search.po.CourseIndex> 课程列表
    */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

 }
