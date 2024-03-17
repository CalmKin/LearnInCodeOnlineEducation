package com.learnincode.learning.service.impl;

import com.learnincode.base.exception.BusinessException;
import com.learnincode.base.model.RestResponse;
import com.learnincode.learning.feignclient.ContentServiceClient;
import com.learnincode.learning.feignclient.MediaServiceClient;
import com.learnincode.learning.feignclient.model.Teachplan;
import com.learnincode.learning.model.dto.OwnedCourseStatusDto;
import com.learnincode.learning.model.po.CoursePublish;
import com.learnincode.learning.service.LearningService;
import com.learnincode.learning.service.MyCourseTablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;


    @Autowired
    MediaServiceClient mediaServiceClient;

    /**
     * @author CalmKin
     * @description 获取某个教学计划对应的视频
     * @version 1.0
     * @date 2024/2/5 11:59
     */
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {

        // =======================先获取课程发布信息，判断是否免费=======================
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);

        if (coursepublish == null) throw new BusinessException("课程不存在");

        String charge = coursepublish.getCharge();


        RestResponse<String> playUrl = mediaServiceClient.getPlayUrlByMediaId(mediaId);
        Teachplan teachPlan = contentServiceClient.getTeachPlanById(teachplanId);

        // 如果是收费课程，那么必须要保证已支付，而且没有过期
        if ("201001".equals(charge)) {
            // 根据课程id和用户id查询学习资格
            OwnedCourseStatusDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);

            //学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            String learnStatus = learningStatus.getLearnStatus();
            if ("702002".equals(learnStatus)) {
                return RestResponse.validfail("无法学习,未选课或选课后未支付");
            }
            if ("702003".equals(learnStatus)) {
                return RestResponse.validfail("课程已过期,请重新购买");
            }
        }

        // 有资格学习（走到这里，要么是免费课程，要么是付费课程有资格）
        return playUrl;
    }
}
