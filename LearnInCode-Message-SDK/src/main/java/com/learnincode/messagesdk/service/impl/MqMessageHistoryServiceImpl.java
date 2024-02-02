package com.learnincode.messagesdk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.messagesdk.mapper.MqMessageHistoryMapper;
import com.learnincode.messagesdk.model.po.MqMessageHistory;
import com.learnincode.messagesdk.service.MqMessageHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *  服务实现类
 */
@Slf4j
@Service
public class MqMessageHistoryServiceImpl extends ServiceImpl<MqMessageHistoryMapper, MqMessageHistory> implements MqMessageHistoryService {

}
