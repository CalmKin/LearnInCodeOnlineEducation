package com.calmkin.learnincode.content.model.po;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 * 课程发布
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("course_pub")
public class CoursePub implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 机构ID
     */
    private Long companyId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 适用人群
     */
    private String users;

    /**
     * 标签
     */
    private String tags;

    /**
     * 创建人
     */
    private String username;

    /**
     * 大分类
     */
    private String mt;

    private String mtName;

    /**
     * 小分类
     */
    private String st;

    private String stName;

    /**
     * 课程等级
     */
    private String grade;

    /**
     * 教育模式(common普通，record 录播，live直播等）
     */
    private String teachmode;

    /**
     * 课程图片
     */
    private String pic;

    /**
     * 课程介绍
     */
    private String description;

    /**
     * 课程营销信息，json格式
     */
    private String market;

    /**
     * 所有课程计划，json格式
     */
    private String teachplan;

    /**
     * 教师信息
     */
    private String teachers;

    /**
     * 发布时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime changeDate;

    /**
     * 是否最新课程
     */
    private Integer isLatest;

    /**
     * 是否发布(0发布 1取消发布)
     */
    private Integer isPub;

    /**
     * 状态（1正常  0删除）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 收费规则，对应数据字典--203
     */
    private String charge;

    /**
     * 现价
     */
    private Float price;

    /**
     * 原价
     */
    private Float priceOld;

    /**
     * 咨询QQ
     */
    private String qq;

    /**
     * 有效性，对应数据字典--204
     */
    private String valid;

    /**
     * 课程有效期-开始时间
     */
    private LocalDateTime startTime;

    /**
     * 课程有效期-结束时间
     */
    private LocalDateTime endTime;


}
