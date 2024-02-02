package com.learnincode.content.feignclient;

import com.learnincode.content.feignclient.fallback.SearchFeignClientFallbackFactory;
import com.learnincode.content.feignclient.model.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "search", fallbackFactory = SearchFeignClientFallbackFactory.class)
public interface SearchFeignClient {
    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);
}
