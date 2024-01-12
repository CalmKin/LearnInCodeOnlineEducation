package com.calmkin.learnincode.base.model;

import lombok.Data;
import lombok.ToString;


 /**
  * @description 分页查询通用参数
  * @author CalmKin
  * @version 1.0
  * @date 2024/1/12 20:28
  */
@Data
@ToString
public class PageParams {

    //当前页码
    private Long pageNo = 1L;

    //每页记录数默认值
    private Long pageSize =10L;

    public PageParams(){

    }
    public PageParams(long pageNo,long pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

}