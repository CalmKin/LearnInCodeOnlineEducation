package com.learnincode.media.service.handler;

import com.learnincode.media.po.MediaProcess;
import com.learnincode.media.service.MediaFileService;
import com.learnincode.media.service.MediaProcessService;
import com.learnincode.media.utils.FileUtils;
import com.learnincode.media.utils.Mp4VideoUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoTask {

    @Autowired
    private MediaProcessService mediaProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path ;

    @XxlJob("videoJobHandler")
    public void processVideo() throws Exception {
        //1. 获取分片id、分片数量
        int shardTotal = XxlJobHelper.getShardTotal();
        int shardIndex = XxlJobHelper.getShardIndex();

        //2. 获取待执行任务列表(获取cpu核心数个任务,多了处理不过来)
        int coreCount = Runtime.getRuntime().availableProcessors();
        List<MediaProcess> taskList = mediaProcessService.getTask(shardIndex, shardTotal, coreCount);

        // 没有任务，直接返回
        if(CollectionUtils.isEmpty(taskList))
        {
            return;
        }

        log.debug("取出待处理视频任务:{}", taskList.size());

        // todo 线程池参数还可以再考虑一下
        // 获取线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(coreCount);

        // CountDownLatch保证所有视频都转码完毕才结束方法
        CountDownLatch latch = new CountDownLatch(coreCount);

        //3.遍历任务列表,提交到线程池进行处理
        taskList.forEach( mediaProcess ->{
            threadPool.execute(
                ()->{
                    try {
                        // 3.1 先获取到任务对应的分布式锁
                        Long taskId = mediaProcess.getId();
                        boolean locked = mediaProcessService.startTask(taskId);

                        // 3.2 如果没有获取到锁，那么没有必要执行下面的转码逻辑
                        if(!locked)
                        {
                            return;
                        }
                        log.debug("开始执行任务:{}", mediaProcess);

                        // 3.3 从minio中下载对应文件(待转码文件)
                        String bucket = mediaProcess.getBucket();
                        String filePath = mediaProcess.getFilePath();
                        String fileId = mediaProcess.getFileId(); // 源文件MD5值
                        File originFile = mediaFileService.downloadFileFromMinIO(bucket, filePath);

                        // 如果下载过程中出现异常，返回的是null
                        if(originFile == null)
                        {
                            log.debug("下载待处理文件失败,originalFile:{}", mediaProcess.getBucket().concat(mediaProcess.getFilePath()));
                            mediaProcessService.saveStatusAfterProcess(taskId,"3", fileId, null, "下载待处理文件失败");
                            return;
                        }

                        // 3.4 对视频进行转码
                        // 创建转码后的临时文件
                        File mp4File  = null;
                        try {
                            mp4File = File.createTempFile("mp4",".mp4");
                        } catch (Exception e) {
                            log.error("创建mp4临时文件失败");
                            mediaProcessService.saveStatusAfterProcess(taskId,"3", fileId, null, "创建临时文件失败");
                            return;
                        }

                        //创建工具类对象
                        String mp4FilePath = mp4File.getAbsolutePath();
                        String result = "";
                        try {
                            Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,originFile.getAbsolutePath(),mp4File.getName(), mp4FilePath);
                            //开始视频转换，成功将返回success
                            result = videoUtil.generateMp4();
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("处理视频文件:{},出错:{}", mediaProcess.getFilePath(), e.getMessage());
                            return;
                        }

                        // 3.5 视频转码失败
                        if(!result.equals("success"))
                        {
                            // 记录失败状态
                            //记录错误信息
                            log.error("处理视频失败,视频地址:{},错误信息:{}", bucket + filePath , result);
                            mediaProcessService.saveStatusAfterProcess(taskId,"3", fileId, null, "视频转码失败");
                            return;
                        }

                        String mp4ObjectName = FILE_UTILS.getBigFilePath(fileId) + ".mp4";
                        // mp4转码后的访问路径
                        String url = "/" + bucket + "/" +mp4ObjectName;

                        // 3.5 转码成功,将文件上传到MinIO
                        mediaFileService.uploadFileToMinIO(bucket,mp4ObjectName, mp4FilePath, FILE_UTILS.getMimeType(mp4FilePath));
                        // 3.6 记录成功状态
                        mediaProcessService.saveStatusAfterProcess(mediaProcess.getId(), "2", fileId, url, null);
                    } finally {

                        // 不管转码成功或者失败，最终都要将计数器 - 1
                        latch.countDown();
                    }

                }
            );
        } );
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        latch.await(30, TimeUnit.MINUTES);

    }

}
