package com.learnincode.content.feignclient.fallback;


import com.learnincode.content.feignclient.MediaFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaFeignClient> {
    @Override
    public MediaFeignClient create(Throwable throwable) {
        log.error("调用媒资管理服务上传文件时发生熔断，异常信息:{}",throwable.toString(),throwable);
        return null;
    }
}