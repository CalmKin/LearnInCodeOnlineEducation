package com.learnincode.ucenter.service.impl;

import com.learnincode.ucenter.model.dto.AuthParamsDto;
import com.learnincode.ucenter.model.dto.UserExt;
import com.learnincode.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("wx_authservice")
@Slf4j
public class WxAuthService implements AuthService {
    @Override
    public UserExt execute(AuthParamsDto authParamsDto) {
        return null;
    }
}
