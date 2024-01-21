package com.learnincode.media.service;


import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.media.dto.QueryMediaParamsDto;
import com.learnincode.media.dto.UploadFileParamsDto;
import com.learnincode.media.dto.UploadFileResultDto;
import com.learnincode.media.po.MediaFiles;


/**
 * @author CalmKin
 * @description 媒资文件管理业务类
 * @version 1.0
 * @date 2024/1/20 21:26
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
 */
 public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


 UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto paramsDto, String filePath);
}
