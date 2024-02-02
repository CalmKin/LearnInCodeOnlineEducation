package com.learnincode.ucenter.feignclient.fallback;

import com.learnincode.ucenter.feignclient.CheckCodeFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CheckCodeFeignFallbackFactory implements FallbackFactory<CheckCodeFeignClient> {
    @Override
    public CheckCodeFeignClient create(Throwable throwable) {
        return new CheckCodeFeignClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("调用验证码服务熔断异常:{}", throwable.getMessage());
                return null;
            }
        };
    }
}
