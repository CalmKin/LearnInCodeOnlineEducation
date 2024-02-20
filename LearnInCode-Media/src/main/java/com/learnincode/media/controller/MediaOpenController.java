package com.learnincode.media.controller;


import com.learnincode.base.exception.BusinessException;
import com.learnincode.base.model.RestResponse;
import com.learnincode.media.po.MediaFiles;
import com.learnincode.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author CalmKin
 * @version 1.0
 * @description 无需登录也能访问媒体相关接口（提供给其他服务访问）
 * @date 2024/1/31 15:56
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    MediaFileService mediaFileService;

    /**
     * 根据媒资id，获取在先访问的url
     *
     * @param mediaId 视频id
     * @return 转码后的url
     */
    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {

        MediaFiles mediaFiles = mediaFileService.getById(mediaId);
        if (mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())) {
            throw new BusinessException("视频还没有转码处理");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }
}