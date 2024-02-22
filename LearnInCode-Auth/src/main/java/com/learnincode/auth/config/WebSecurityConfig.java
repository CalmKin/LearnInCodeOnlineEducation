package com.learnincode.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全管理配置，定义了认证管理器、密码编码器以及 HTTP 请求的安全拦截机制
 */
// 启用了 Spring Security 的 Web 安全功能
@EnableWebSecurity
//启用了全局方法安全性。
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    //配置用户信息服务(从内存中查询用户信息)
//    @Bean
//    public UserDetailsService userDetailsService() {
//        //这里配置用户信息,这里暂时使用这种方式将用户存储在内存中
//        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//        manager.createUser(User.withUsername("zhangsan").password("123").authorities("p1").build());
//        manager.createUser(User.withUsername("lisi").password("456").authorities("p2").build());
//        return manager;
//    }

    @Autowired
    DaoAuthenticationProviderCustom daoAuthenticationProviderCustom;

    // 将DaoAuthenticationProvider设置为自定义的
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //将自定义的 DaoAuthenticationProvider 设置为认证管理器，用于处理身份验证。
        auth.authenticationProvider(daoAuthenticationProviderCustom);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * @author CalmKin
     * @description 密码编码器
     * @version 1.0
     * @date 2024/2/2 11:41
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
//        //密码为明文方式
//        return NoOpPasswordEncoder.getInstance();
        // 使用BCrypt加密
        return new BCryptPasswordEncoder();
    }

    //配置安全拦截机制
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                //访问/r开始的请求需要认证通过
                .antMatchers("/r/**").authenticated()
                //其它请求全部放行
                .anyRequest().permitAll()
                .and()
                //登录成功跳转到/login-success
                .formLogin().successForwardUrl("/login-success");
    }


}
