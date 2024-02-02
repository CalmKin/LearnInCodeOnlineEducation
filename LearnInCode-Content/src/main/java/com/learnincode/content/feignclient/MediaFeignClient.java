package com.learnincode.content.feignclient;


import com.learnincode.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author CalmKin
 * @description 媒资管理服务远程接口
 * @version 1.0
 * @date 2024/2/1 16:41
 */
@FeignClient(value = "media",configuration = MultipartSupportConfig.class)
public interface MediaFeignClient {
    // 上传文件接口
    @RequestMapping(value = "/media/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("filedata") MultipartFile upload, @RequestParam(value = "objectName",required=false) String objectName);

}
