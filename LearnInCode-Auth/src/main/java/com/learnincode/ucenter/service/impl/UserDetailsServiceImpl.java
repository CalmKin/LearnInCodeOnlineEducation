package com.learnincode.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.ucenter.mapper.MenuMapper;
import com.learnincode.ucenter.mapper.UserMapper;
import com.learnincode.ucenter.model.dto.AuthParamsDto;
import com.learnincode.ucenter.model.dto.UserExt;
import com.learnincode.ucenter.model.po.Menu;
import com.learnincode.ucenter.model.po.User;
import com.learnincode.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    MenuMapper menuMapper;

    /**
     * @author CalmKin
     * @description 根据账号查询用户信息, 并封装成UserDetails返回
     * @version 1.0
     * @date 2024/2/2 16:49
     */
    @Override
    public UserDetails loadUserByUsername(String Str) throws UsernameNotFoundException {

        //将认证参数转为AuthParamsDto类型
        AuthParamsDto paramsDto = null;
        try {
            paramsDto = JSON.parseObject(Str, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", Str);
            throw new BusinessException("认证请求数据格式不对");
        }

        // 认证类型
        String authType = paramsDto.getAuthType();
        // 统一入口实现多种认证方式的核心
        AuthService authService = (AuthService) applicationContext.getBean( authType+ "_authservice");

        // 开始认证(抽象接口，由具体的认证方法实现)
        UserExt userExt = authService.execute(paramsDto);

        // 根据用户认证信息,封装userDetail
        UserDetails userDetails = getUserPrincipal(userExt);

        return userDetails;
    }



    /**
     * @author CalmKin
     * @description  将UserExt封装为UserDetails
     * @version 1.0
     * @date 2024/2/22 18:06
     */
    private UserDetails getUserPrincipal(UserExt userExt) {
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
//        String[] authorities = {"test"};
        String password = userExt.getPassword();

        // 从数据库中查询用户包含权限
        List<Menu> menus = menuMapper.selectPermissionByUserId(userExt.getId());
        List<String> permission = new ArrayList<>();

        //将用户权限码取出放到permission列表中
        if(!CollectionUtils.isEmpty(menus))
        {
            menus.forEach(item -> {
                permission.add(item.getCode());
            });
        }
        else
            //如果不加则报Cannot pass a null GrantedAuthority collection
            permission.add("simple");

        //将用户权限放在UserExt中
        userExt.setPermissions(permission);
        // 转化成数组,方便保存在security上下文
        String[] authorities = permission.toArray(new String[0]);

        //为了安全在令牌中不放密码，排除敏感信息
        excludeSensitiveInfo(userExt);
        // 将用户信息压缩成JSON串存在UserName中
        String userInfo = JSON.toJSONString(userExt);
        //创建UserDetails对象
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(userInfo)
                .password(password).authorities(authorities).build();
        return userDetails;
    }


    /**
     * @author CalmKin
     * @description 排除user中的敏感信息
     * @version 1.0
     * @date 2024/3/13 10:38
     */
    void excludeSensitiveInfo(User user) {
        user.setPassword(null);
        user.setEmail(null);
        user.setQq(null);
        user.setCellphone(null);
    }

}
