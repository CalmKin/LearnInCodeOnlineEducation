package com.learnincode.content.feignclient.fallback;


import com.learnincode.base.model.RestResponse;
import com.learnincode.content.feignclient.MediaFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaFeignClient> {
    @Override
    public MediaFeignClient create(Throwable throwable) {
        return new MediaFeignClient() {
            @Override
            public String uploadFile(MultipartFile upload, String objectName) {
                log.error("调用媒资管理服务上传文件时发生熔断，异常信息:{}",throwable.toString(),throwable);
                return null;
            }

            @Override
            public RestResponse<String> getPlayUrlByMediaId(String mediaId) {
                log.error("获取在线播放地址时发生熔断，异常信息:{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
