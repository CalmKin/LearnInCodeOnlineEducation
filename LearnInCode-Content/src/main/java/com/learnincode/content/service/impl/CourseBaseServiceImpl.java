package com.learnincode.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.content.mapper.CourseBaseMapper;
import com.learnincode.content.model.dto.QueryCourseParamsDto;
import com.learnincode.content.model.po.CourseBase;
import com.learnincode.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 86158
* @description 针对表【course_base(课程基本信息)】的数据库操作Service实现
* @createDate 2024-01-17 11:30:41
*/
@Service
@Slf4j
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase>
    implements CourseBaseService {


    /**
     * 课程分页查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 课程查询条件（课程名称，审核状态，发布状态）
     * @return 分页返回值
     */
    @Override
    public PageResult<CourseBase> pageCourseBase(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        // 构造查询条件
        LambdaQueryWrapper<CourseBase> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        lqw.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        lqw.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());


        Long size = pageParams.getPageSize();
        Long offset = pageParams.getPageNo();
        Page<CourseBase> page = new Page<>(offset,size);


        Page<CourseBase> res = page(page, lqw);
        List<CourseBase> records = res.getRecords();
        log.info("分页查询到课程记录为:{}",records);

        long total = res.getTotal();

        return new PageResult<>(records,total,offset,size);
    }
}




