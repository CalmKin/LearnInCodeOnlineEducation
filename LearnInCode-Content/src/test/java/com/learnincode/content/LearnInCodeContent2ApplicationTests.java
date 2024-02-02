package com.learnincode.content;

import com.learnincode.content.config.MultipartSupportConfig;
import com.learnincode.content.feignclient.MediaFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@SpringBootTest
class LearnInCodeContent2ApplicationTests {

    @Autowired
    MediaFeignClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() {

        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\Java_Sourse\\test.html"));
        mediaServiceClient.uploadFile(multipartFile,"course/test.html");
    }


}
