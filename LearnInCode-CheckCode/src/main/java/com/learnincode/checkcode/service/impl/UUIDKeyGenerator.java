package com.learnincode.checkcode.service.impl;

import com.learnincode.checkcode.service.CheckCodeService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @description uuid生成器
 */
@Component("UUIDKeyGenerator")
public class UUIDKeyGenerator implements CheckCodeService.KeyGenerator {
    @Override
    public String generate(String prefix) {
        String uuid = UUID.randomUUID().toString();
        return prefix + uuid.replaceAll("-", "");
    }
}
