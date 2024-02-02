package com.learnincode.ucenter.service;


import com.learnincode.ucenter.model.dto.AuthParamsDto;
import com.learnincode.ucenter.model.dto.UserExt;

/**
 * @author CalmKin
 * @description 统一认证接口,后续要支持不同认证方式直接实现接口即可
 * @version 1.0
 * @date 2024/2/2 20:14
 */
public interface AuthService {

    UserExt execute(AuthParamsDto authParamsDto);
}
