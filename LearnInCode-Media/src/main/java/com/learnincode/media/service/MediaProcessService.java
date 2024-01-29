package com.learnincode.media.service;

import com.learnincode.media.po.MediaProcess;

import java.util.List;

public interface MediaProcessService {

    List<MediaProcess> getTask(int executorId,int totalExecutor, int taskCnt);

}
