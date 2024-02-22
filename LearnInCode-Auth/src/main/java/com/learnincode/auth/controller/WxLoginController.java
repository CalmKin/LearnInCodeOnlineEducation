package com.learnincode.auth.controller;

import com.learnincode.ucenter.model.po.User;
import com.learnincode.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @description 微信登录
 */
@Slf4j
@RestController
public class WxLoginController {

    @Autowired
    WxAuthService wxAuthService;


    /**
     * @author CalmKin
     * @description 微信登录请求接口，这个接口地址是我们一开始就设置好了的，当用户同意，认证通过之后，就会带着code和state访问这个接口
     * @version 1.0
     * @date 2024/2/22 18:55
     */
    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}",code,state);
        //请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库
        User user = wxAuthService.wxAuth(code);
        if(user==null){
            // 用户不存在，返回错误页面
            return "redirect:http://www.learnincode.cn/error.html";
        }
        String username = user.getUsername();

        // 返回登录页面
        return "redirect:http://www.learnincode.cn/sign.html?username="+username+"&authType=wx";
    }



}
