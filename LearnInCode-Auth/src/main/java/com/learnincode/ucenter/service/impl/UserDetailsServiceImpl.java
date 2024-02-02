package com.learnincode.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.ucenter.mapper.UserMapper;
import com.learnincode.ucenter.model.dto.AuthParamsDto;
import com.learnincode.ucenter.model.dto.UserExt;
import com.learnincode.ucenter.model.po.User;
import com.learnincode.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    ApplicationContext applicationContext;

    /**
     * @author CalmKin
     * @description 根据账号查询用户信息, 并封装成UserDetails返回
     * @version 1.0
     * @date 2024/2/2 16:49
     */
    @Override
    public UserDetails loadUserByUsername(String Str) throws UsernameNotFoundException {

        AuthParamsDto paramsDto = null;
        try {
            paramsDto = JSON.parseObject(Str, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", Str);
            throw new BusinessException("认证请求数据格式不对");
        }

        // 统一入口实现多种认证方式的核心
        AuthService authService = (AuthService) applicationContext.getBean(paramsDto.getAuthType() + "_authservice");

        // 开始认证
        UserExt userExt = authService.execute(paramsDto);

        // 根据用户认证信息,封装userDetail
        UserDetails userDetails = getUserPrincipal(userExt);

        return userDetails;
    }

    /**
     * @param userExt 认证通过之后获取到的用户信息
     * @return
     */

    private UserDetails getUserPrincipal(UserExt userExt) {
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities = {"test"};

        String password = userExt.getPassword();

        // 排除敏感信息
        excludeSensitiveInfo(userExt);
        // 将用户信息压缩成JSON串存在UserName中
        String userInfo = JSON.toJSONString(userExt);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(userInfo)
                .password(password).authorities(authorities).build();
        return userDetails;
    }

    void excludeSensitiveInfo(User user) {
        user.setPassword(null);
        user.setEmail(null);
        user.setQq(null);
        user.setCellphone(null);
    }

}
