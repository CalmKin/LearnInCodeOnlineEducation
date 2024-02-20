package com.learnincode.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learnincode.content.model.po.TeachplanMedia;

/**
* @author 86158
* @description 针对表【teachplan_media】的数据库操作Service
* @createDate 2024-01-20 09:01:36
*/
public interface TeachplanMediaService extends IService<TeachplanMedia> {

    void detachMedia(Long teachPlanId, Long mediaId);
}
