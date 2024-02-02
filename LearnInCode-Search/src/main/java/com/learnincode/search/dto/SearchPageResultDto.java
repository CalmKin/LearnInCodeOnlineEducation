package com.learnincode.search.dto;

import com.learnincode.base.model.PageResult;
import lombok.Setter;

import java.util.List;

@Setter
public class SearchPageResultDto<T> extends PageResult {

    //大分类列表
    List<String> mtList;
    //小分类列表
    List<String> stList;


    public SearchPageResultDto(List<T> items, long counts, long page, long pageSize) {
        super(items, counts, page, pageSize);
    }

}
