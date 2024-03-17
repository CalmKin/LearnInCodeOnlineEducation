package com.learnincode.content.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
// 标记这个服务，允许对外提供资源
@EnableResourceServer
// 开启全局方法访问认证
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class ResouceServerConfig extends ResourceServerConfigurerAdapter {


    //资源服务标识
    public static final String RESOURCE_ID = "learn-in-code";

    @Autowired
    TokenStore tokenStore;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID)//资源 id
                .tokenStore(tokenStore)
                .stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                // 由网关统一认证，服务自己无需认证
//                .antMatchers("/r/**","/course/**").authenticated()//所有/r/**的请求必须认证通过
                .anyRequest().permitAll()
        ;
    }

}