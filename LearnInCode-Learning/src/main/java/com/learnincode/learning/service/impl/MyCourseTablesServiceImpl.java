package com.learnincode.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.learning.feignclient.ContentServiceClient;
import com.learnincode.learning.mapper.ChooseCourseMapper;
import com.learnincode.learning.mapper.CourseTablesMapper;
import com.learnincode.learning.model.dto.ChooseCourseDto;
import com.learnincode.learning.model.po.ChooseCourse;
import com.learnincode.learning.model.po.CoursePublish;
import com.learnincode.learning.model.po.CourseTables;
import com.learnincode.learning.service.MyCourseTablesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

public class MyCourseTablesServiceImpl implements MyCourseTablesService {


    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    ChooseCourseMapper chooseCourseMapper;

    @Autowired
    CourseTablesMapper courseTablesMapper;
    
    /**
     * @author CalmKin
     * @description 添加课程到我的选课表中,
     * @version 1.0
     * @date 2024/2/3 17:44
     */
    @Override
    public ChooseCourseDto addChooseCourse(String userId, Long courseId) {

        // 先从已发布课程表里面查询对应课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);

        if (coursepublish == null) throw new BusinessException("课程不存在");

        // 获取收费信息
        String charge = coursepublish.getCharge();

        // 如果是免费课程
        if("201000".equals(charge))
        {
            // 添加免费课程
            ChooseCourse chooseCourse = addFreeCoruse(userId, coursepublish);
            // 添加我的课程表

        }
        else
        {
            // 添加收费课程到选课记录表
        }

        return null;
    }


    /**
     * @author CalmKin
     * @description 添加免费课程,免费课程加入选课记录表、我的课程表
     * @version 1.0
     * @date 2024/2/3 17:52
     */
    public ChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {

        // 因为没有主键约束，所以先查询是否存在免费且选课成功的记录
        LambdaQueryWrapper<ChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(ChooseCourse::getUserId, userId)
                .eq(ChooseCourse::getCourseId, coursepublish.getId())
                .eq(ChooseCourse::getOrderType, "700001")//免费课程
                .eq(ChooseCourse::getStatus, "701001");//选课成功

        List<ChooseCourse> chooseCourses = chooseCourseMapper.selectList(queryWrapper);

        // 已经有了，直接返回，不进行插入
        if(chooseCourses != null && chooseCourses.size()>0)
        {
            return chooseCourses.get(0);
        }

        // 往选课记录插入
        ChooseCourse chooseCourse = new ChooseCourse();
        chooseCourse.setCourseId(coursepublish.getId());    // 设置课程id
        chooseCourse.setCourseName(coursepublish.getName());//课程名称
        chooseCourse.setCoursePrice(0f);//免费课程价格为0
        chooseCourse.setUserId(userId); // 用户id
        chooseCourse.setCompanyId(coursepublish.getCompanyId()); // 课程所属机构id
        chooseCourse.setOrderType("700001");//免费课程
        LocalDateTime now = LocalDateTime.now();
        chooseCourse.setCreateDate(now);
        chooseCourse.setStatus("701001");//选课状态：选课成功

        chooseCourse.setValidDays(365);//免费课程默认365
        chooseCourse.setValidtimeStart(now); // 生效时间
        chooseCourse.setValidtimeEnd(now.plusDays(365)); // 结束时间
        chooseCourseMapper.insert(chooseCourse);

        return chooseCourse;
    }


    /**
     *  添加到我的课程表
     * @param chooseCourse 选课记录
     * @return
     */
    public CourseTables addMyCourseTables(ChooseCourse chooseCourse)
    {
        // 先进行判断，只有选课成功且没有过期的课程才能添加
        String status = chooseCourse.getStatus();
        if(!"701001".equals(status))
        {
            throw new BusinessException("选课未成功,无法添加到课程表");
        }

        String userId = chooseCourse.getUserId();
        Long courseId = chooseCourse.getCourseId();

        // 因为有主键约束，所以需要先查询是否已经存在，没有再执行插入
        CourseTables courseTables = courseTablesMapper.
                selectOne(new LambdaQueryWrapper<CourseTables>()
                        .eq(CourseTables::getUserId, userId)
                        .eq(CourseTables::getCourseId, courseId));
        if(courseTables != null) return courseTables;


        CourseTables courseTablesNew = new CourseTables();
        BeanUtils.copyProperties(chooseCourse, courseTablesNew);
        // 补全字段
        courseTablesNew.setChooseCourseId(chooseCourse.getId());    // 选课
        courseTablesNew.setCourseType(chooseCourse.getOrderType()); // 选课类型
        courseTablesNew.setUpdateDate(LocalDateTime.now());

        return courseTablesNew;


    }

}
