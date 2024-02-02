package com.learnincode.ucenter.feignclient;


import com.learnincode.ucenter.feignclient.fallback.CheckCodeFeignFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @author CalmKin
 * @description 验证码服务远程调用接口
 * @version 1.0
 * @date 2024/2/2 21:58
 */
@FeignClient(value = "checkcode", fallbackFactory = CheckCodeFeignFallbackFactory.class)
@RequestMapping("/checkcode")
public interface CheckCodeFeignClient {
    @PostMapping(value = "/verify")
    Boolean verify(String key, String code);
}
