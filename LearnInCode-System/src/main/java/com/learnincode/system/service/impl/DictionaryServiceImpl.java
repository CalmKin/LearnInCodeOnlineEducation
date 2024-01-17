package com.learnincode.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.system.mapper.DictionaryMapper;
import com.learnincode.system.model.Dictionary;
import com.learnincode.system.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements DictionaryService {

    /**
     * @return 查询系统数据字典列表
     */
    @Override
    public List<Dictionary> queryAll() {

        List<Dictionary> list = this.list();


        return list;
    }

    /**
     *
     * @param code -- String 数据字典Code
     * @return
     */
    @Override
    public Dictionary getByCode(String code) {


        LambdaQueryWrapper<Dictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dictionary::getCode, code);

        Dictionary dictionary = this.getOne(queryWrapper);


        return dictionary;
    }
}
