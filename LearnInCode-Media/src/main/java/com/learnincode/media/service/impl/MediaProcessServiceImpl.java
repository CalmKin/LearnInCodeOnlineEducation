package com.learnincode.media.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.media.mapper.MediaFilesMapper;
import com.learnincode.media.mapper.MediaProcessHistoryMapper;
import com.learnincode.media.mapper.MediaProcessMapper;
import com.learnincode.media.po.MediaFiles;
import com.learnincode.media.po.MediaProcess;
import com.learnincode.media.po.MediaProcessHistory;
import com.learnincode.media.service.MediaProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MediaProcessServiceImpl extends ServiceImpl<MediaProcessMapper, MediaProcess> implements MediaProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getTask(int executorId, int totalExecutor, int taskCnt) {
        return mediaProcessMapper.getTaskList(executorId, totalExecutor, taskCnt);
    }

    /**
     *
     * @param taskId
     * @return
     */
    @Override
    public boolean startTask(long taskId)
    {
        int cnt = mediaProcessMapper.tryLock(taskId);
        return cnt<=0 ? false : true;
    }

    @Override
    @Transactional
    public void saveStatusAfterProcess(Long TaskId, String status, String fileId, String Url, String errMsg) {
        // 先找到对应任务
        MediaProcess task = getById(TaskId);
        // 健壮性判断
        if(task == null) return;

        // 如果任务处理失败，更新任务表
        if(status.equals("3"))
        {
            task.setErrormsg(errMsg);
            task.setFailCount( task.getFailCount() + 1);
            task.setStatus("3");
            // todo 如果失败次数超过三次
            updateById(task);
            return;
        }

        // 任务执行成功
        // 1. 改文件表，设置url
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(Url);
        mediaFilesMapper.updateById(mediaFiles);
        // 2. 改任务表和历史任务表
        // 2.1 插入历史任务表
        MediaProcessHistory history = new MediaProcessHistory();
        BeanUtils.copyProperties(task, history);
        mediaProcessHistoryMapper.insert(history);

        // 2.2 从任务表中删除
        removeById(task);
    }
}
