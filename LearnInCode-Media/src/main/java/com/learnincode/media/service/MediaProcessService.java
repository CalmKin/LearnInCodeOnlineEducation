package com.learnincode.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learnincode.media.po.MediaProcess;

import java.util.List;

public interface MediaProcessService extends IService<MediaProcess> {

    List<MediaProcess> getTask(int executorId,int totalExecutor, int taskCnt);

    List<MediaProcess> getDeadTask(int executorId, int totalExecutor, int taskCnt);

    boolean startTask(long taskId);

    /**
     * 任务执行结束之后，更新任务状态
     * @param TaskId 任务id
     * @param status 任务执行状态
     * @param fileId 任务对应文件id（因为还要写文件表）
     * @param Url 如果转码成功，会有一个可直接访问的url
     * @param errMsg 如果转码失败，会有错误信息
     */
    void saveStatusAfterProcess(Long TaskId, String status, String fileId,String Url, String errMsg);

}
