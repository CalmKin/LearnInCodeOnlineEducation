package com.learnincode.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.base.model.RestResponse;
import com.learnincode.media.dto.QueryMediaParamsDto;
import com.learnincode.media.dto.UploadFileParamsDto;
import com.learnincode.media.dto.UploadFileResultDto;
import com.learnincode.media.mapper.MediaFilesMapper;
import com.learnincode.media.mapper.MediaProcessMapper;
import com.learnincode.media.po.MediaFiles;
import com.learnincode.media.po.MediaProcess;
import com.learnincode.media.service.MediaFileService;
import com.learnincode.media.utils.FileUtils;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class MediaFileServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    // 普通文件桶
    @Value("${minio.bucket.files}")
    private String file_bucket;

    // 视频桶
    @Value("${minio.bucket.videofiles}")
    private String video_bucket;


    /**
     * @author CalmKin
     * @description 分页查询媒资信息
     * @version 1.0
     * @date 2024/1/21 14:39
     */
    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        String filename = queryMediaParamsDto.getFilename();
        String fileType = queryMediaParamsDto.getFileType();
        String auditStatus = queryMediaParamsDto.getAuditStatus();

        queryWrapper.like(StringUtils.isNotEmpty(filename), MediaFiles::getFilename, filename);
        queryWrapper.like(StringUtils.isNotEmpty(fileType), MediaFiles::getFilename, fileType);
        queryWrapper.like(StringUtils.isNotEmpty(auditStatus), MediaFiles::getFilename, auditStatus);

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    /**
     * @param companyId 机构Id
     * @param paramsDto 上传文件的参数
     * @param filePath  文件路径
     * @return
     */
    @Override
//    @Transactional        // 包含网络请求，比较耗时
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto paramsDto, String filePath) {
        // 先判断文件是否存在
        File file = new File(filePath);
        if (!file.exists()) throw new BusinessException("文件不存在");

        // 第一部分，上传到MinIO
        String md5Hex = FILE_UTILS.getMd5Hex(filePath);
        String objectName = FILE_UTILS.getBucketObjectName(filePath);
        String mimeType = FILE_UTILS.getMimeType(filePath);

        MediaFiles mediaFiles = mediaFilesMapper.selectById(md5Hex);

        // 如果文件已经上传过了，就不再上传和入库了
        if (mediaFiles == null) {
            // 第一步失败,抛异常回滚事务
            boolean step1 = uploadFileToMinIO(file_bucket, objectName, filePath, mimeType);
            if (!step1) throw new BusinessException("文件上传失败,请稍后重试");
            // 为了防止事务失效,获取代理对象
            MediaFileService serviceProxy = (MediaFileService) AopContext.currentProxy();
            // 第二部分，保存到数据库
            mediaFiles = serviceProxy.saveFileToDB(companyId, md5Hex, paramsDto, file_bucket, objectName);
        }
        // 拷贝属性返回
        UploadFileResultDto ret = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, ret);
        return ret;
    }

    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象名称
     * @description 将文件信息添加到文件表
     */
    @Transactional
    @Override
    public MediaFiles saveFileToDB(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);

            mediaFiles.setCompanyId(companyId);
            // 文件访问路径 = host/桶名/objectName
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            // 这个是在MinIO中的文件路径
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}", mediaFiles.toString());
                throw new BusinessException("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());
            MediaFileService proxy = (MediaFileService) AopContext.currentProxy();
            proxy.addWaitingTask(mediaFiles);
            log.debug("保存待处理任务成功,{}", mediaFiles.toString());
        }
        return mediaFiles;
    }
    public void addWaitingTask(MediaFiles mediaFiles)
    {
        // 根据文件mimeType判断是否需要转码
        String mimeType = FILE_UTILS.getMimeType(mediaFiles.getFilePath());

        // todo 后续通过在配置文件，以列表的形式指定需要转码的文件mimetype类型，业务层判断当前文件mimetype是否在列表里面
        // 如果是avi类型
        if(mimeType.equals("video/x-msvideo"))
        {
            // 创建待处理任务对象
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);

            // 设置任务状态(未处理)
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            // 初始失败次数为0
            mediaProcess.setFailCount(0);
            // url是转码后的，现在还没转码，设置为空
            mediaProcess.setUrl(null);

            //插入待处理任务表
            mediaProcessMapper.insert(mediaProcess);
        }

    }



    /**
     * @author CalmKin
     * @description 上传大型文件之前，检查文件是否存在
     * @version 1.0
     * @date 2024/1/22 15:35
     */
    @Override
    public RestResponse<Boolean> checkfile(String fileMd5) {
        MediaFiles files = getById(fileMd5);
        // 数据库里面已经存在，再去minio里面检查是否存在
        if (files != null) {
            //桶
            String bucket = files.getBucket();
            //存储目录
            String filePath = files.getFilePath();

            try {
                GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(filePath)
                        .build());

                // minio里面已经存在了
                if(object != null) return RestResponse.success(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 大文件不在数据库或者minio，此时才会继续走下一步检查分块
        return RestResponse.success(false);
    }


    /**
     * @author CalmKin
     * @description
     * 检查某个分块是否存在,分块不存在时,前端才会请求上传分块的接口
     * @param fileMd5 大文件的md5
     * @version 1.0
     * @date 2024/1/22 16:09
     */
    @Override
    public RestResponse<Boolean> checkchunk(String fileMd5, int chunkOrder) {
        // 分块文件夹路径
        String chunkPath = FILE_UTILS.getBigFilePath(fileMd5) + "/chunk/" + chunkOrder;
        GetObjectResponse object = null;
        try {
             object = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(video_bucket)
                    .object(chunkPath)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("分块文件不存在", chunkOrder);
        }
        // 分块存在
        if(object != null) return RestResponse.success(true);
        // 分块不存在
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadchunk(String fileMd5, int chunkOrder,  String tmpFilePath) {

        String chunkPath = FILE_UTILS.getBigFilePath(fileMd5) + "/chunk/" + chunkOrder;

       String mimeType = FILE_UTILS.getMimeType(tmpFilePath);

        try {
            //将分块存储至minIO
            uploadFileToMinIO(video_bucket, chunkPath, tmpFilePath, mimeType);
            return RestResponse.success(true);
        }catch (Exception ex)
        {
            ex.printStackTrace();
            log.debug("上传分块文件:{},失败:{}",tmpFilePath,ex.getMessage());
        }
        return RestResponse.validfail(false,"上传分块失败");
    }

    /**
     * @param fileMd5
     * @param chunkTotal
     * @return
     */
    @Override
    public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto)
    {

        //=====================1. 合并分块=====================
        // 获取分块存储的目录
        String chunkDir = FILE_UTILS.getBigFilePath(fileMd5) + "/chunk/";

        // 获取分块列表
        ArrayList<ComposeSource> args = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            ComposeSource arg = ComposeSource.builder()
                    .bucket(video_bucket)
                    .object(chunkDir + i)
                    .build();
            args.add(arg);
        }

        // 合并后的文件扩展名
        String filename = uploadFileParamsDto.getFilename();
        String extName = filename.substring(filename.lastIndexOf('.'));
        // 合并后的文件名 = md5[0] + md5[1] + md5.ext
        String mergeFilePath = FILE_UTILS.getBigFilePath(fileMd5) + extName;
        try {
            // 合并文件
            ComposeObjectArgs res = ComposeObjectArgs.builder()
                    .bucket(video_bucket)
                    .object(mergeFilePath)   // 合并之后的文件名
                    .sources(args)
                    .build();
            ObjectWriteResponse response = minioClient.composeObject(res);
            log.debug("合并文件成功:{}",mergeFilePath);

        } catch (Exception e) {
            log.debug("合并文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "合并文件失败。");
        }


        try {
            // =====================2.校验文件完整性=====================
            StatObjectArgs objectStat = StatObjectArgs.builder().bucket(video_bucket).object(mergeFilePath).build();

            // 获取minio上面的文件元数据
            StatObjectResponse fileInfo = minioClient.statObject(objectStat);
            String cloudHash = fileInfo.etag();

            if(!cloudHash.equals(fileMd5))
            {
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
            }
            // 设置文件大小
            uploadFileParamsDto.setFileSize(fileInfo.size());

        } catch (Exception e) {
            log.debug("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        }

        // =====================3.文件入库=====================
        MediaFileService proxy = (MediaFileService) AopContext.currentProxy();
        proxy.saveFileToDB(companyId, fileMd5, uploadFileParamsDto, video_bucket, mergeFilePath);

        // =====================4.清除分块文件=====================
        clearChunks(fileMd5,chunkTotal);

        return RestResponse.success(true);
    }

    /**
     * 将文件上传到MinIO
     *
     * @param bucket
     * @param objectName
     * @param filePath
     * @param mimeType
     * @return
     */
    @Override
    public boolean uploadFileToMinIO(String bucket, String objectName, String filePath, String mimeType) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)   // 指定桶
                    .object(objectName)    //指定对象名（可以随便取）
                    .filename(filePath)   // 指定本地文件路径
                    .contentType(mimeType)  // 指定文件格式
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件上传出错,bucket{},objectName{},出错信息{}", bucket, objectName, e.getMessage());
            return false;
        }
        log.error("文件上传成功,bucket{},objectName{},出错信息{}", bucket, objectName);
        return true;
    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    @Override
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            // 从minio获取文件输入流（读取）
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件，将文件流拷贝到临时文件（输出到临时文件）
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            // 输入流写入到输出流（读取输入流中的内容，写入到输出流）
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * @author CalmKin
     * @description 将所有分块文件清除
     * @version 1.0
     * @date 2024/1/23 9:26
     */
    public void clearChunks(String md5Hex, int chunkSize)
    {

        try {
            List<DeleteObject> list = new ArrayList<>();
            for(int i=0; i<chunkSize; i++)
            {
                String chunkPath = FILE_UTILS.getBigFilePath(md5Hex) + "/chunk/" + i;
                DeleteObject deleteObject = new DeleteObject(chunkPath);
                list.add(deleteObject);
            }
            RemoveObjectsArgs args = RemoveObjectsArgs.builder().bucket(video_bucket).objects(list).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(args);

            // minio的一个特性，要get之后才能真正删掉分块
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清除分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清除分块文件失败,chunkFileFolderPath:{}",e.getMessage());
        }


    }


}
