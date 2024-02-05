package com.learnincode.learning.service.impl;


import com.alibaba.fastjson.JSON;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.learning.config.PayNotifyConfig;
import com.learnincode.learning.service.MyCourseTablesService;
import com.learnincode.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author CalmKin
 * @description 监听订单服务队列，接收支付结果
 * @version 1.0
 * @date 2024/2/5 9:55
 */
@Service
@Slf4j
public class ReceivePayNotifyService {

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message)
    {

        // =====================从消息体里面取出自定义的消息=====================
        byte[] body = message.getBody();
        String myMqMsg = new String(body);
        MqMessage mqMessage = JSON.parseObject(myMqMsg, MqMessage.class);


        // ===================== 根据businessKey,进行响应的业务处理=====================
        // 消息类型,payresult_notify表示是支付结果通知类型的消息
        String messageType = mqMessage.getMessageType();
        // 选课id
        String courseId = mqMessage.getBusinessKey1();
        // 订单类型,60201表示购买课程
        String orderType = mqMessage.getBusinessKey2();

        // 如果收到的消息类型或者不是自己管的消息,就不进行处理
        if( !PayNotifyConfig.MESSAGE_TYPE.equals(messageType) || !"60201".equals(orderType))
        {
            return;
        }

        // 收到了支付成功的消息，接下来将选课状态保存进数据库
        boolean flag = myCourseTablesService.saveChooseCourseStauts(courseId);
        if(!flag)
        {
            //添加选课失败，抛出异常，消息重回队列
            throw new BusinessException("收到支付结果,保存选课状态失败");
        }

    }


}
