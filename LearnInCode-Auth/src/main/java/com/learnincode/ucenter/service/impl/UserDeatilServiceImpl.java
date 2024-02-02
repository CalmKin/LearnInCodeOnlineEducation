package com.learnincode.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnincode.ucenter.mapper.UserMapper;
import com.learnincode.ucenter.model.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDeatilServiceImpl implements UserDetailsService {

    @Autowired
    UserMapper userMapper;


    /**
     * @author CalmKin
     * @description 根据账号查询用户信息, 并封装成UserDetails返回
     * @version 1.0
     * @date 2024/2/2 16:49
     */
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userName));

        //返回空表示用户不存在
        if (user == null) return null;

        //取出数据库存储的正确密码
        String password = user.getPassword();

        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities = {"test"};

        // 排除敏感信息
        excludeSensitiveInfo(user);
        // 将用户信息压缩成JSON串存在UserName中
        String userInfo = JSON.toJSONString(user);

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
