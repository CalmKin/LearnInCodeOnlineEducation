package com.learnincode.media.service.impl;

import com.learnincode.media.mapper.MediaProcessMapper;
import com.learnincode.media.po.MediaProcess;
import com.learnincode.media.service.MediaProcessService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MediaProcessServiceImpl implements MediaProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Override
    public List<MediaProcess> getTask(int executorId, int totalExecutor, int taskCnt) {
        return mediaProcessMapper.getTaskList(executorId, totalExecutor, taskCnt);
    }
}
