package com.learnincode.learning.controller;

import com.learnincode.base.model.RestResponse;
import com.learnincode.learning.service.LearningService;
import com.learnincode.learning.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 我的学习接口
 */
@Api(value = "学习过程管理接口", tags = "学习过程管理接口")
@Slf4j
@RestController
public class MyLearningController {


    @Autowired
    LearningService learningService;

    @ApiOperation("获取视频在线播放地址")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getvideo(@PathVariable("courseId") Long courseId, @PathVariable("teachplanId") Long teachplanId, @PathVariable("mediaId") String mediaId) {

        SecurityUtil.User user = SecurityUtil.getUser();
        // 这里的逻辑是一刀切，不管用户没有登录的话，免费视频也不能看
        if(user == null) return RestResponse.validfail("请先登录");
        String userId = user.getId();

        return learningService.getVideo(userId, courseId, teachplanId, mediaId);

    }

}
