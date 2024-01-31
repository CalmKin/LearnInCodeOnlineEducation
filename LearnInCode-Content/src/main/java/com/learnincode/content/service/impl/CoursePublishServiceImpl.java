package com.learnincode.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.content.mapper.CourseMarketMapper;
import com.learnincode.content.mapper.CoursePublishPreMapper;
import com.learnincode.content.model.dto.CourseBaseInfoDto;
import com.learnincode.content.model.dto.CoursePreviewDto;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.model.po.CourseBase;
import com.learnincode.content.model.po.CourseMarket;
import com.learnincode.content.model.po.CoursePublishPre;
import com.learnincode.content.service.CourseBaseService;
import com.learnincode.content.service.CoursePublishService;
import com.learnincode.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Override
    public void commitAudit(Long companyId, Long courseId) {

        //================基础校验================

        //约束校验
        CourseBase courseBase = courseBaseService.getById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前课程审核状态为已提交,不允许再次提交
        if("202003".equals(auditStatus)){
            throw new BusinessException("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            throw new BusinessException("不允许提交其它机构的课程。");
        }

        //课程图片是否填写
        if(StringUtils.isEmpty(courseBase.getPic())){
            throw new BusinessException("提交失败，请上传课程图片");
        }

        //================设置预发布信息================
        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //设置基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfoDtoById(courseId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);


        //保存课程营销信息（转为json）
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);


        //保存课程计划信息（转为json）
        List<TeachplanDto> teachplanTree = teachplanService.getTeachPlanTree(courseId);
        if(teachplanTree.size()<=0){
            throw new BusinessException("提交失败，还没有添加课程计划");
        }
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());


        //================预发布信息同步到预发布表（不存在则添加，已存在则更新）================
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //================更新课程基本表的审核状态================
        courseBase.setAuditStatus("202003");
        courseBaseService.updateById(courseBase);

        // todo ================将审核结果写入课程审核记录================

    }


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto viewObject = new CoursePreviewDto();
        // 查询课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfoDtoById(courseId);
        viewObject.setCourseBase(courseBaseInfo);

        // 查询课程计划
        List<TeachplanDto> teachPlans = teachplanService.getTeachPlanTree(courseId);
        viewObject.setTeachplans(teachPlans);

        return viewObject;
    }
}
