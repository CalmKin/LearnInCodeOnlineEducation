package com.learnincode.learning.feignclient.fallback;

import com.learnincode.learning.feignclient.ContentServiceClient;
import com.learnincode.learning.feignclient.model.Teachplan;
import com.learnincode.learning.model.po.CoursePublish;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @description TODO
 */
@Slf4j
@Component
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {

            @Override
            public CoursePublish getCoursepublish(Long courseId) {
                log.error("调用内容管理服务发生熔断:{}", throwable.toString(),throwable);
                return null;
            }

            @Override
            public Teachplan getTeachPlanById(Long teachPlanId) {
                log.error("调用内容管理服务发生熔断:{}", throwable.toString(),throwable);
                return null;
            }
        };
    }
}
