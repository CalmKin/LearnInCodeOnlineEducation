package com.learnincode.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.content.mapper.TeachplanMediaMapper;
import com.learnincode.content.model.po.TeachplanMedia;
import com.learnincode.content.service.TeachplanMediaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author 86158
* @description 针对表【teachplan_media】的数据库操作Service实现
* @createDate 2024-01-20 09:01:36
*/
@Service
public class TeachplanMediaServiceImpl extends ServiceImpl<TeachplanMediaMapper, TeachplanMedia>
    implements TeachplanMediaService{

    /**
     * @author CalmKin
     * @description 解除课程计划和视频资源的绑定
     * @version 1.0
     * @date 2024/2/20 16:12
     */
    @Override
    @Transactional
    public void detachMedia(Long teachPlanId, Long mediaId) {
        // 根据课程计划id和绑定媒资id，找到关系表中的记录

        update(new LambdaUpdateWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, teachPlanId)
                .eq(TeachplanMedia::getMediaId,mediaId));

    }

}




