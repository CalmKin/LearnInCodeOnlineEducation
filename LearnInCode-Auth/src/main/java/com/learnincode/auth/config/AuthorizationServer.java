package com.learnincode.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import javax.annotation.Resource;

/**
 * @description 授权服务器配置
 */
 @Configuration
 @EnableAuthorizationServer
 public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {

  @Resource(name="authorizationServerTokenServicesCustom")
  private AuthorizationServerTokenServices authorizationServerTokenServices;

 @Autowired
 private AuthenticationManager authenticationManager;


  //客户端详情服务
  @Override
  public void configure(ClientDetailsServiceConfigurer clients)
          throws Exception {
        //客户端详情信息存储在内存中(和SpringSecurity指定用户信息在内存一样,方便开发测试)
       // 如果要定义多个的话，就要复制多份下面的方法
        clients.inMemory()
                //定义一个客户端，其 ID 为 "WebApp"。
                .withClient("WebApp")
//                .secret("WebApp")
                // WebApp通过BCrypt编码之后，作为客户端token密钥
                .secret(new BCryptPasswordEncoder().encode("WebApp"))
                // 指定该客户端可以访问的资源 ID
                .resourceIds("learn-in-code")
                // 定义该客户端允许的授权类型
                .authorizedGrantTypes("authorization_code", "password","client_credentials","implicit","refresh_token")// 该client允许的授权类型authorization_code,password,refresh_token,implicit,client_credentials
                // 定义了客户端可以访问的授权范围。
                .scopes("all")
                //指定了是否自动批准授权，设置为 false 则需要用户手动授权。
                .autoApprove(false)
                //客户端接收授权码的重定向地址
                .redirectUris("http://www.learnincode.cn")
   ;
  }


  //令牌端点的访问配置
  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
   endpoints
           //指定认证管理器,用于验证用户的身份。
           .authenticationManager(authenticationManager)
           //指定了令牌管理服务，用于创建和管理访问令牌,这里用我们自定义的
           .tokenServices(authorizationServerTokenServices)
           //允许访问令牌端点的 HTTP 请求方法为 POST。
           .allowedTokenEndpointRequestMethods(HttpMethod.POST);
  }

  //令牌端点的安全配置
  @Override
  public void configure(AuthorizationServerSecurityConfigurer security){
   security
           //表示允许任何用户访问 OAuth2 令牌公开端点（/oauth/token_key）获取令牌。
           .tokenKeyAccess("permitAll()")
           //表示允许任何用户访问 OAuth2 令牌验证端点（/oauth/check_token）验证令牌。
           .checkTokenAccess("permitAll()")
           //允许客户端通过表单提交进行令牌申请。
           .allowFormAuthenticationForClients()
   ;
  }



 }
