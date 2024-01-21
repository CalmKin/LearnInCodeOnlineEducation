package com.learnincode.media.dto;

import lombok.Data;



/**
 * @author CalmKin
 * @description 上传文件请求参数
 * @version 1.0
 * @date 2024/1/21 14:41
 */
@Data
public class UploadFileParamsDto {

    /**
     * 文件名称
     */
    private String filename;


    /**
     * 文件类型（文档，音频，视频）
     */
    private String fileType;
    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 标签
     */
    private String tags;

    /**
     * 上传人
     */
    private String username;

    /**
     * 备注
     */
    private String remark;



}