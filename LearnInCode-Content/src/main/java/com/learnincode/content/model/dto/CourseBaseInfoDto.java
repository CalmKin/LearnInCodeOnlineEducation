package com.learnincode.content.model.dto;


import com.learnincode.content.model.po.CourseBase;
import lombok.Data;


/**
 * @author CalmKin
 * @description 课程基本信息（基本信息+营销信息）
 * @version 1.0
 * @date 2024/1/18 19:14
 */
@Data
public class CourseBaseInfoDto extends CourseBase {

 /**
  * 收费规则，对应数据字典
  */
 private String charge;

 /**
  * 价格
  */
 private Float price;


 /**
  * 原价
  */
 private Float originalPrice;

 /**
  * 咨询qq
  */
 private String qq;

 /**
  * 微信
  */
 private String wechat;

 /**
  * 电话
  */
 private String phone;

 /**
  * 有效期天数
  */
 private Integer validDays;

 /**
  * 大分类名称
  */
 private String mtName;

 /**
  * 小分类名称
  */
 private String stName;

}
