package com.learnincode.ucenter.service;


import com.learnincode.ucenter.model.po.User;

/**
 * @author CalmKin
 * @description 微信认证接口
 * @version 1.0
 * @date 2024/2/22 19:00
 */
public interface WxAuthService {
    User wxAuth(String code);
}
