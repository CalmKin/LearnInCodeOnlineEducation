package com.learnincode.auth.config;


import com.learnincode.ucenter.service.impl.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * @author CalmKin
 * @version 1.0
 * @description 自定义DaoAuthenticationProvider, 支持不同的认证类型
 * @date 2024/2/2 19:08
 */
@Component
@Slf4j
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {


    // setter注入自定义的UserDeatilService
    @Autowired
    public void setUserDetailsService(UserDetailsServiceImpl userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    /**
     * @author CalmKin
     * @description 屏蔽密码对比的方式
     * @version 1.0
     * @date 2024/2/2 19:10
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }
}
