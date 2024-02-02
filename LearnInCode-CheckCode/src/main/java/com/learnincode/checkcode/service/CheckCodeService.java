package com.learnincode.checkcode.service;

import com.learnincode.checkcode.model.CheckCodeParamsDto;
import com.learnincode.checkcode.model.CheckCodeResultDto;

/**
 * @description 验证码接口
 */
public interface CheckCodeService {


    /**
     * @description 生成验证码
     * @param checkCodeParamsDto 生成验证码参数
    */
     CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

     /**
      * @description 校验验证码
      * @param key
      * @param code
      * @return boolean
     */
    public boolean verify(String key, String code);


    /**
     * @description 验证码生成器
    */
    public interface CheckCodeGenerator{
        /**
         * 验证码生成
         * @return 验证码
         */
        String generate(int length);
        

    }

    /**
     * @description key生成器
     */
    public interface KeyGenerator{

        /**
         * key生成
         * @return 验证码
         */
        String generate(String prefix);
    }


    /**
     * @description 验证码存储
     */
    public interface CheckCodeStore{

        /**
         * @description 向缓存设置key
         * @param key key
         * @param value value
         * @param expire 过期时间,单位秒
         * @return void
        */
        void set(String key, String value, Integer expire);

        String get(String key);

        void remove(String key);
    }
}
