package com.learnincode.ucenter.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.ucenter.mapper.UserMapper;
import com.learnincode.ucenter.model.dto.AuthParamsDto;
import com.learnincode.ucenter.model.dto.UserExt;
import com.learnincode.ucenter.model.po.User;
import com.learnincode.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * @author CalmKin
 * @description 账号名密码认证方式
 * @version 1.0
 * @date 2024/2/2 20:17
 */
@Service("password_authservice")
@Slf4j
public class PasswordAuthService implements AuthService {

    @Autowired
    private UserMapper userMapper;

    // 注入编码器
    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserExt execute(AuthParamsDto authParamsDto) {

        String userName = authParamsDto.getUsername();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userName));

        //返回空表示用户不存在
        if (user == null)
        {
            throw new BusinessException("用户不存在");
        }

        //取出数据库存储的正确密码
        String correctPassword = user.getPassword();
        // 输入的密码
        String inputPassword = authParamsDto.getPassword();

        boolean matches = encoder.matches(inputPassword, correctPassword);

        // 密码校验失败
        if(!matches)
        {
            throw new BusinessException("用户名或密码错误");
        }

        UserExt userExt = new UserExt();
        BeanUtils.copyProperties(user, userExt);

        return userExt;
    }
}
