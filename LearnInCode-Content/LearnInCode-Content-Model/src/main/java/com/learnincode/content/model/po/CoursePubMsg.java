package com.learnincode.content.model.po;

import java.io.Serializable;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 * 
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("course_pub_msg")
public class CoursePubMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程发布id
     */
    private Long pubId;

    /**
     * 课程发布名称
     */
    private String pubName;

    /**
     * 课程发布消息状态(0:未发送，1:已发送)
     */
    private Integer pubStatus;

    /**
     * 课程基本信息id
     */
    private Long courseId;

    /**
     * 教学机构id
     */
    private Long companyId;


}
