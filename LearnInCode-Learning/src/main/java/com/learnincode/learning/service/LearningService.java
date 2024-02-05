package com.learnincode.learning.service;

import com.learnincode.base.model.RestResponse;

public interface LearningService {
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);

}
