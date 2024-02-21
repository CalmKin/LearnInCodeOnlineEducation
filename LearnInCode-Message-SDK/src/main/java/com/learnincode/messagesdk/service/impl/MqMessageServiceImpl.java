package com.learnincode.messagesdk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.messagesdk.mapper.MqMessageHistoryMapper;
import com.learnincode.messagesdk.mapper.MqMessageMapper;
import com.learnincode.messagesdk.model.po.MqMessage;
import com.learnincode.messagesdk.model.po.MqMessageHistory;
import com.learnincode.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *  服务实现类
 */
@Slf4j
@Service
public class MqMessageServiceImpl extends ServiceImpl<MqMessageMapper, MqMessage> implements MqMessageService {

    @Autowired
    MqMessageMapper mqMessageMapper;

    @Autowired
    MqMessageHistoryMapper mqMessageHistoryMapper;



    /**
     * @author CalmKin
     * @description 根据xxl-job作业分片索引 + 分片总数，获取当前分片领取的任务
     * @version 1.0
     * @date 2024/2/20 20:16
     */
    @Override
    public List<MqMessage> getMessageList(int shardIndex, int shardTotal, String messageType,int count) {
        return mqMessageMapper.selectListByShardIndex(shardTotal,shardIndex,messageType,count);
    }


    /**
     * @author CalmKin
     * @description 将业务消息保存到数据库中
     * @version 1.0
     * @date 2024/2/20 20:15
     */
    @Override
    public MqMessage addMessage(String messageType, String businessKey1, String businessKey2, String businessKey3) {
        MqMessage mqMessage = new MqMessage();
        mqMessage.setMessageType(messageType);
        mqMessage.setBusinessKey1(businessKey1);
        mqMessage.setBusinessKey2(businessKey2);
        mqMessage.setBusinessKey3(businessKey3);
        int insert = mqMessageMapper.insert(mqMessage);
        if(insert>0){
            return mqMessage;
        }else{
            return null;
        }

    }

    /**
     * 1. 将任务状态标记为完成
     * 2. 添加到历史消息表
     * 3. 删除原来表中的记录
     * @param id 消息id
     * @return
     */
    @Transactional
    @Override
    public int completed(long id) {
        MqMessage mqMessage = new MqMessage();
        //完成任务
        mqMessage.setState("1");
        int update = mqMessageMapper.update(mqMessage, new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId, id));
        // 状态更新成功之后,才进行后续操作
        if(update>0){

            mqMessage = mqMessageMapper.selectById(id);
            //添加到历史表
            MqMessageHistory mqMessageHistory = new MqMessageHistory();
            BeanUtils.copyProperties(mqMessage,mqMessageHistory);
            mqMessageHistoryMapper.insert(mqMessageHistory);
            //删除消息表
            mqMessageMapper.deleteById(id);
            return 1;
        }
        return 0;

    }

    @Override
    public int completedStageOne(long id) {
        MqMessage mqMessage = new MqMessage();
        //完成阶段1任务
        mqMessage.setStageState1("1");
        return mqMessageMapper.update(mqMessage,new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId,id));
    }

    @Override
    public int completedStageTwo(long id) {
        MqMessage mqMessage = new MqMessage();
        //完成阶段2任务
        mqMessage.setStageState2("1");
        return mqMessageMapper.update(mqMessage,new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId,id));
    }

    @Override
    public int completedStageThree(long id) {
        MqMessage mqMessage = new MqMessage();
        //完成阶段3任务
        mqMessage.setStageState3("1");
        return mqMessageMapper.update(mqMessage,new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId,id));
    }

    @Override
    public int completedStageFour(long id) {
        MqMessage mqMessage = new MqMessage();
        //完成阶段4任务
        mqMessage.setStageState4("1");
        return mqMessageMapper.update(mqMessage,new LambdaQueryWrapper<MqMessage>().eq(MqMessage::getId,id));
    }

    @Override
    public int getStageOne(long id) {
        return Integer.parseInt(mqMessageMapper.selectById(id).getStageState1());
    }

    @Override
    public int getStageTwo(long id) {
        return Integer.parseInt(mqMessageMapper.selectById(id).getStageState2());
    }

    @Override
    public int getStageThree(long id) {
        return Integer.parseInt(mqMessageMapper.selectById(id).getStageState3());
    }

    @Override
    public int getStageFour(long id) {
        return Integer.parseInt(mqMessageMapper.selectById(id).getStageState4());
    }


}
