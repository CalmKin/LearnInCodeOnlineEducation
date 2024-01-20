package com.learnincode.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.media.dto.QueryMediaParamsDto;
import com.learnincode.media.mapper.MediaFilesMapper;
import com.learnincode.media.po.MediaFiles;
import com.learnincode.media.service.MediaFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


 @Service
public class MediaFileServiceImpl implements MediaFileService {

  @Autowired
  MediaFilesMapper mediaFilesMapper;

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
}
