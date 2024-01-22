package com.learnincode.media.controller;


import com.learnincode.base.model.RestResponse;
import com.learnincode.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


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

    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5)
            throws Exception {
        return fileService.checkfile(fileMd5);
    }


    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
        return null;
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {

        return null;
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        return null;

    }

}
