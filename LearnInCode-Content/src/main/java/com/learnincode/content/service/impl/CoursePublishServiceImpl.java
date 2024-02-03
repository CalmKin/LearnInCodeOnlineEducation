package com.learnincode.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.base.exception.CommonError;
import com.learnincode.content.config.MultipartSupportConfig;
import com.learnincode.content.feignclient.MediaFeignClient;
import com.learnincode.content.mapper.CourseMarketMapper;
import com.learnincode.content.mapper.CoursePublishMapper;
import com.learnincode.content.mapper.CoursePublishPreMapper;
import com.learnincode.content.model.dto.CourseBaseInfoDto;
import com.learnincode.content.model.dto.CoursePreviewDto;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.model.po.CourseBase;
import com.learnincode.content.model.po.CourseMarket;
import com.learnincode.content.model.po.CoursePublish;
import com.learnincode.content.model.po.CoursePublishPre;
import com.learnincode.content.service.CourseBaseService;
import com.learnincode.content.service.CoursePublishService;
import com.learnincode.content.service.TeachplanService;
import com.learnincode.messagesdk.model.po.MqMessage;
import com.learnincode.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CoursePublishServiceImpl  extends ServiceImpl<CoursePublishMapper, CoursePublish>
        implements CoursePublishService {

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    MediaFeignClient mediaFeignClient;

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

    @Override
    @Transactional
    public void coursepublish(Long companyId, Long courseId) {
        // 查询预发布表
        CoursePublishPre preCourse = coursePublishPreMapper.selectById(courseId);
        if(preCourse == null) throw new BusinessException("请先提交课程审核，审核通过才可以发布");

        CourseBase courseBase = courseBaseService.getById(courseId);
        // 课程审核通过方可发布
        if(!("202004".equals(courseBase.getAuditStatus()))) throw new BusinessException("审核未通过，不能发布");

        if(!companyId.equals(courseBase.getCompanyId())) throw new BusinessException("不允许提交其它机构的课程");

        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(preCourse, coursePublish);

        // 更新课程状态为已发布
        CoursePublish course = coursePublishMapper.selectById(courseId);

        // 如果发布表里面没有，执行插入
        if(course == null) coursePublishMapper.insert(coursePublish);
        else coursePublishMapper.updateById(coursePublish);

        // 更新课程状态为已发布
        courseBase.setStatus("203002");
        courseBaseService.updateById(courseBase);

        // 向消息表写入消息
        saveCoursePublishMessage(courseId);

        // 删除预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);

    }

    @Override
    public File generateCourseHtml(Long courseId) {

        File htmlFile = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/template/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(2L);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //输出流
            htmlFile = File.createTempFile(String.valueOf(courseId),".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            // 一定要往外抛异常，否则调用方(调度任务)会将这个子任务标记为已完成
            throw new BusinessException("课程静态化异常");
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\Java_Sourse\\test.html"));
        String result = mediaFeignClient.uploadFile(multipartFile, "course/test.html");

        // 如果走了降级逻辑,一定要及时抛异常
        if(result == null)
        {
            throw new BusinessException("静态页面上传失败");
        }

    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }

    /**
     * @description 保存消息表记录
     * @param courseId  课程id
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            throw new BusinessException(CommonError.UNKOWN_ERROR.toString());
        }

    }

}
