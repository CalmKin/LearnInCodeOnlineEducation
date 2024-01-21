package com.learnincode.media.controller;

import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.media.dto.QueryMediaParamsDto;
import com.learnincode.media.dto.UploadFileResultDto;
import com.learnincode.media.po.MediaFiles;
import com.learnincode.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description 媒资文件管理接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
 @Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
 @RestController
public class MediaFilesController {


  @Autowired
  MediaFileService mediaFileService;


 @ApiOperation("媒资列表查询接口")
 @PostMapping("/files")
 public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
  Long companyId = 1232141425L;
  return mediaFileService.queryMediaFiles(companyId,pageParams,queryMediaParamsDto);

 }
 /**
  * @author CalmKin
  * @description
  * 请求内容：Content-Type: multipart/form-data;
  * form-data; name="filedata"; filename="具体的文件名称"
  * @version 1.0
  * @date 2024/1/21 11:42
  */
 @ApiOperation("上传文件接口")
 @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
 public UploadFileResultDto uploadFile(@RequestPart("filedata") MultipartFile upload)
 {
     Long companyId = 1232141425L;
     return  null;
 }

}
