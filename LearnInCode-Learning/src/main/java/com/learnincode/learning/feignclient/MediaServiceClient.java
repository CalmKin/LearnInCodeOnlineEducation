package com.learnincode.learning.feignclient;

import com.learnincode.learning.feignclient.fallback.MediaServiceClientFallbackFactory;
import com.learnincode.base.model.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @author CalmKin
 * @description 媒资服务远程调用接口
 * @version 1.0
 * @date 2024/2/3 16:16
 */
 @FeignClient(value = "media-api",fallbackFactory = MediaServiceClientFallbackFactory.class)
 @RequestMapping("/media")
 public interface MediaServiceClient {

  @GetMapping("/open/preview/{mediaId}")
  public RestResponse<String> getPlayUrlByMediaId(@PathVariable("mediaId") String mediaId);

 }
