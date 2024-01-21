package com.learnincode.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.media.dto.QueryMediaParamsDto;
import com.learnincode.media.dto.UploadFileParamsDto;
import com.learnincode.media.dto.UploadFileResultDto;
import com.learnincode.media.mapper.MediaFilesMapper;
import com.learnincode.media.po.MediaFiles;
import com.learnincode.media.service.MediaFileService;
import com.learnincode.media.utils.FileUtils;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

  @Autowired
  MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

 @Override
 public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
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

  queryWrapper.like(StringUtils.isNotEmpty(filename),MediaFiles::getFilename, filename);
  queryWrapper.like(StringUtils.isNotEmpty(fileType),MediaFiles::getFilename, fileType);
  queryWrapper.like(StringUtils.isNotEmpty(auditStatus),MediaFiles::getFilename, auditStatus);

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
     * 将文件上传到MinIO
     * @param bucket
     * @param objectName
     * @param filePath
     * @param mimeType
     * @return
     */
    public boolean uploadFileToMinIO(String bucket, String objectName,String filePath, String mimeType)
    {
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
            log.error("文件上传出错,bucket{},objectName{},出错信息{}",bucket,objectName,e.getMessage());
            return false;
        }
        log.error("文件上传成功,bucket{},objectName{},出错信息{}",bucket,objectName);
        return true;
    }
}
