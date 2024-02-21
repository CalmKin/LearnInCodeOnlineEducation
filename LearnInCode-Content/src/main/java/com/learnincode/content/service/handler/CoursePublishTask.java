package com.learnincode.content.service.handler;

import com.learnincode.base.exception.BusinessException;
import com.learnincode.content.feignclient.SearchFeignClient;
import com.learnincode.content.feignclient.model.CourseIndex;
import com.learnincode.content.model.po.CoursePublish;
import com.learnincode.content.service.CoursePublishService;
import com.learnincode.messagesdk.model.po.MqMessage;
import com.learnincode.messagesdk.service.MessageProcessAbstract;
import com.learnincode.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    private CoursePublishService coursePublishService;

    @Autowired
    private SearchFeignClient searchFeignClient;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);

        //分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }


    /**
     * 单个课程发布的业务逻辑
     * @param mqMessage 执行任务内容
     * @return
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        // 课程id
        Long courseId = Long.valueOf(mqMessage.getBusinessKey1());

        //课程静态化
        generateCourseHtml(mqMessage,courseId);
        //课程索引
        saveCourseIndex(mqMessage,courseId);
        //课程缓存
        saveCourseCache(mqMessage,courseId);

        return true;
    }

    //生成课程静态化页面并上传至文件系统
    public void generateCourseHtml(MqMessage mqMessage,long courseId){

        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageOne(id);

        // 如果某个阶段已经完成了，那么不用再执行了
        if(stageOne >0){
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }

        // 生成静态页面
        File file = coursePublishService.generateCourseHtml(courseId);
        // 上传到Minio
        if(file!=null)
        {
            // 如果执行失败，会抛异常，不会执行下面的保存状态
            coursePublishService.uploadCourseHtml(courseId,file);
        }

        //保存第一阶段状态
        mqMessageService.completedStageOne(id);

    }

    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        // todo 缓存课程信息
        log.debug("将课程信息缓存至redis,课程id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage,long courseId){
        log.debug("保存课程索引信息,课程id:{}",courseId);

        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageTwo = mqMessageService.getStageTwo(id);
        if(stageTwo > 0){
            log.debug("课程索引已处理直接返回，课程id:{}",courseId);
            return ;
        }

        // 真正进行保存
        Boolean result = saveCourseIndex(courseId);
        if(result){
            //保存第二阶段
            mqMessageService.completedStageTwo(id);
        }
    }

    private Boolean saveCourseIndex(Long courseId) {

        //发布之后的课程信息
        CoursePublish coursePublish = coursePublishService.getById(courseId);

        //拷贝至课程索引对象
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);

        //远程调用搜索服务api添加课程信息到索引
        Boolean add = searchFeignClient.add(courseIndex);
        if(!add){
            throw new BusinessException("添加索引失败");
        }
        return add;
    }

}
