package com.learnincode.media.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.base.model.RestResponse;
import com.learnincode.media.dto.QueryMediaParamsDto;
import com.learnincode.media.dto.UploadFileParamsDto;
import com.learnincode.media.dto.UploadFileResultDto;
import com.learnincode.media.po.MediaFiles;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author CalmKin
 * @description 媒资文件管理业务类
 * @version 1.0
 * @date 2024/1/20 21:26
 */
public interface MediaFileService extends IService<MediaFiles> {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
 */
 public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


 UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto paramsDto, String filePath);

    @Transactional
    MediaFiles saveFileToDB(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);

    RestResponse<Boolean> checkfile(String fileMd5);

    RestResponse<Boolean> checkchunk(String fileMd5, int chunk);
}
