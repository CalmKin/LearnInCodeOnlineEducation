package com.learnincode.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnincode.ucenter.mapper.UserMapper;
import com.learnincode.ucenter.mapper.UserRoleMapper;
import com.learnincode.ucenter.model.dto.AuthParamsDto;
import com.learnincode.ucenter.model.dto.UserExt;
import com.learnincode.ucenter.model.po.User;
import com.learnincode.ucenter.model.po.UserRole;
import com.learnincode.ucenter.service.AuthService;
import com.learnincode.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


/**
 * @author CalmKin
 * @description 微信扫码认证方式
 * @version 1.0
 * @date 2024/2/22 18:56
 */
@Service("wx_authservice")
@Slf4j
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    UserRoleMapper userRoleMapper;

    @Autowired
    WxAuthServiceImpl currentProxy;

    @Autowired
    RestTemplate restTemplate;

    // 指定微信认证所需参数
    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;

    /**
     * @author CalmKin
     * @description 根据
     * @version 1.0
     * @date 2024/2/22 19:00
     */
    @Override
    public User wxAuth(String code) {

        // 通过code申请access_token
        Map<String, String> accessTokenMap = getAccess_token(code);

        if(accessTokenMap == null) return null;

        //拿着token,请求微信获取用户相关信息
        String openid = accessTokenMap.get("openid");
        String access_token = accessTokenMap.get("access_token");

        Map<String, String> userinfo = getUserinfo(access_token, openid);
        if(userinfo==null){
            return null;
        }

        User user = currentProxy.addWxUser(userinfo);

        return user;
    }

    // todo 拿access_token向微信请求查询用户信息
    private Map<String, String> getUserinfo(String accessToken, String openid) {

        return null;
    }

    /**
     * 通过微信返回的授权码code，申请访问令牌
     * 响应示例
     {
     "access_token":"ACCESS_TOKEN",
     "expires_in":7200,
     "refresh_token":"REFRESH_TOKEN",
     "openid":"OPENID",
     "scope":"SCOPE",
     "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     }
     */
    private Map<String,String> getAccess_token(String code) {

        // 申请令牌请求地址
        String wxUrl_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //请求微信地址
        String wxUrl = String.format(wxUrl_template, appid, secret, code);

        log.info("调用微信接口申请access_token, url:{}", wxUrl);

        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);

        // 防止乱码进行转码
        String result = new  String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        Map<String,String> resultMap = JSON.parseObject(result, Map.class);

        return resultMap;
    }



    /**
     * @author CalmKin
     * @description 根据unionId,在本业务数据库中添加对应用户信息和用户角色,如果已经存在则直接返回
     * @version 1.0
     * @date 2024/2/22 19:21
     */
    @Transactional
    public User addWxUser(Map userInfoMap)
    {
        // 获取用户的unionid
        String unionid = userInfoMap.get("unionid").toString();

        // 根据unionid查询数据库
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getWxUnionid, unionid));

        if(user!=null) return user;

        String userId = UUID.randomUUID().toString();
        user = new User();
        user.setId(userId);
        user.setWxUnionid(unionid);

        //=====================填充用户信息=====================
        //记录从微信得到的昵称
        user.setNickname(userInfoMap.get("nickname").toString());
        user.setUserpic(userInfoMap.get("headimgurl").toString());
        user.setName(userInfoMap.get("nickname").toString());
        user.setUsername(unionid);
        user.setPassword(unionid);
        //用户类型: 学生
        user.setUtype("101001");
        //用户状态
        user.setStatus("1");
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);


        //=====================插入用户角色=====================
        UserRole userRole = new UserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setUserId(userId);
        //用户角色:学生
        userRole.setRoleId("17");
        userRoleMapper.insert(userRole);

        return user;
    }






    /**
     * @author CalmKin
     * @description 策略模式抽象方法，认证的入口
     * @version 1.0
     * @date 2024/2/22 19:21
     */

    @Override
    public UserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();

        // 根据用户名查询用户信息
        User user = userMapper
                .selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if(user==null){
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }
        UserExt UserExt = new UserExt();
        BeanUtils.copyProperties(user,UserExt);
        return UserExt;
    }
}
