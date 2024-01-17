package com.learnincode.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;




/**
 * @author CalmKin
 * @description 前端所需数据字典
 * @version 1.0
 * @date 2024/1/17 16:24
 */
@Data
@TableName("dictionary")
public class Dictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据字典项--json格式
     [{
     "sd_name": "低级",
     "sd_id": "200001",
     "sd_status": "1"
     }, {
     "sd_name": "中级",
     "sd_id": "200002",
     "sd_status": "1"
     }, {
     "sd_name": "高级",
     "sd_id": "200003",
     "sd_status": "1"
     }]
     */

    /**
     * id标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 数据字典名称
     */
    private String name;

    /**
     * 数据字典代码
     */
    private String code;


    private String itemValues;


}
