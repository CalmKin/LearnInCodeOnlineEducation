package com.learnincode.media.controller;


import com.learnincode.base.model.RestResponse;
import com.learnincode.media.dto.UploadFileParamsDto;
import com.learnincode.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


/**
 * @author CalmKin
 * @version 1.0
 * @description 大文件上传接口
 * @date 2024/1/22 15:32
 */
@RestController
@RequestMapping("/upload")
@Api(value = "大文件上传接口", tags = "大文件上传接口")
public class BigFileController {

    @Autowired
    private MediaFileService fileService;

    @ApiOperation(value = "文件上传前检查大文件是否已经存在")
    @PostMapping("/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5) {
        return fileService.checkfile(fileMd5);
    }


    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
        return fileService.checkchunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunkOrder) throws Exception {
        // 先把前端传过来的文件保存到临时文件
        File tmpFile = File.createTempFile(fileMd5.substring(0, 10) + chunkOrder, ".tmp");
        file.transferTo(tmpFile);

        String absolutePath = tmpFile.getAbsolutePath();
        return fileService.uploadchunk(fileMd5, chunkOrder, absolutePath);
    }


    /**
     * @param chunkTotal 这个字段是为了方便后端遍历minio分块列表的时候，提供一个循环边界
     * @author CalmKin
     * @description 合并minio中所有分块
     * @version 1.0
     * @date 2024/1/22 16:52
     */
    @ApiOperation(value = "合并文件")
    @PostMapping("/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {

        Long companyId = 1232141425L;
        UploadFileParamsDto dto = new UploadFileParamsDto();

        dto.setFileType("001002");
        dto.setTags("课程视频");
        dto.setRemark("");
        dto.setFilename(fileName);


        return fileService.mergechunks(companyId, fileMd5, chunkTotal, dto);
    }

}
