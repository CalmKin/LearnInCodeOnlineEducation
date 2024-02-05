package com.learnincode.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.learning.feignclient.ContentServiceClient;
import com.learnincode.learning.mapper.ChooseCourseMapper;
import com.learnincode.learning.mapper.OwnedCourseMapper;
import com.learnincode.learning.model.dto.ChoosedCourseDto;
import com.learnincode.learning.model.dto.OwnedCourseStatusDto;
import com.learnincode.learning.model.po.ChoosedCourse;
import com.learnincode.learning.model.po.CoursePublish;
import com.learnincode.learning.model.po.OwnedCourse;
import com.learnincode.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MyCourseTablesServiceImpl implements MyCourseTablesService {


    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    ChooseCourseMapper chooseCourseMapper;

    @Autowired
    OwnedCourseMapper ownedCourseMapper;

    /**
     * @author CalmKin
     * @description 添加课程到我的选课表中
     * @version 1.0
     * @date 2024/2/3 17:44
     */
    @Override
    @Transactional
    public ChoosedCourseDto addChooseCourse(String userId, Long courseId) {

        // 先从已发布课程表里面查询对应课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);

        if (coursepublish == null) throw new BusinessException("课程不存在");

        // 获取收费信息
        String charge = coursepublish.getCharge();

        ChoosedCourse choosedCourse = null;
        // 如果是免费课程
        if ("201000".equals(charge)) {
            // 添加免费课程
            choosedCourse = addCoruse(userId, coursepublish, false);
            // 添加我的课程表

        } else {
            // 添加收费课程到选课记录表
            choosedCourse = addCoruse(userId, coursepublish, true);
        }

        ChoosedCourseDto choosedCourseDto = new ChoosedCourseDto();
        OwnedCourseStatusDto learningStatus = getLearningStatus(userId, courseId);
        BeanUtils.copyProperties(learningStatus, choosedCourseDto);

        return choosedCourseDto;
    }


    /**
     * @author CalmKin
     * @description 判断学习资格
     * 学习资格状态 [{"code":"702001","desc":"正常学习"},
     * {"code":"702002","desc":"没有选课或选课后没有支付"},
     * {"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * @version 1.0
     * @date 2024/2/3 18:54
     */
    @Override
    public OwnedCourseStatusDto getLearningStatus(String userId, Long courseId) {

        // 查询我的课程表,因为有些课程是拥有过但是过期了的
        OwnedCourse ownedCourse = getOwnedCourse(userId, courseId);

        OwnedCourseStatusDto ownedCourseStatus = null;
        // 如果查不到，说明没有选或者选完后没有支付
        if (ownedCourse == null) {
            ownedCourseStatus = new OwnedCourseStatusDto();
            //没有选课或选课后没有支付
            ownedCourseStatus.setLearnStatus("702002");
            return ownedCourseStatus;
        }

        // 如果能查到，还要检查课程有没有过期
        BeanUtils.copyProperties(ownedCourse, ownedCourseStatus);
        // 如果已经过期
        boolean expired = ownedCourse.getValidtimeEnd().isBefore(LocalDateTime.now());

        String learnStatus = expired ? "702003" : "702001";

        ownedCourseStatus.setLearnStatus(learnStatus);

        return ownedCourseStatus;
    }

    /**
     * @author CalmKin
     * @description 添加课程加入选课记录表
     * @version 1.0
     * @date 2024/2/3 17:52
     */
    public ChoosedCourse addCoruse(String userId, CoursePublish coursepublish, boolean isCharge) {

        String orderType = isCharge ? "700002" : "700001";
        String courseInitStatus = isCharge ? "701002" : "701001";

        // 因为没有主键约束，所以先查询是否存在免费且选课成功的记录
        LambdaQueryWrapper<ChoosedCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(ChoosedCourse::getUserId, userId)
                .eq(ChoosedCourse::getCourseId, coursepublish.getId())
                .eq(ChoosedCourse::getOrderType, orderType)//免费课程
                .eq(ChoosedCourse::getStatus, courseInitStatus);//选课成功

        List<ChoosedCourse> choosedCours = chooseCourseMapper.selectList(queryWrapper);

        // 已经有了，直接返回，不进行插入
        if (choosedCours != null && choosedCours.size() > 0) {
            return choosedCours.get(0);
        }

        // 往选课记录插入
        ChoosedCourse choosedCourse = new ChoosedCourse();
        choosedCourse.setCourseId(coursepublish.getId());    // 设置课程id
        choosedCourse.setCourseName(coursepublish.getName());//课程名称
        choosedCourse.setCoursePrice(0f);//免费课程价格为0
        choosedCourse.setUserId(userId); // 用户id
        choosedCourse.setCompanyId(coursepublish.getCompanyId()); // 课程所属机构id
        choosedCourse.setOrderType(orderType);//免费课程
        LocalDateTime now = LocalDateTime.now();
        choosedCourse.setCreateDate(now);
        choosedCourse.setStatus(courseInitStatus);//选课状态：选课成功

        choosedCourse.setValidDays(365);//免费课程默认365
        choosedCourse.setValidtimeStart(now); // 生效时间
        choosedCourse.setValidtimeEnd(now.plusDays(365)); // 结束时间
        chooseCourseMapper.insert(choosedCourse);

        return choosedCourse;
    }


    /**
     * 添加到我的课程表
     * @param choosedCourse 选课记录
     * @return
     */
    public OwnedCourse addMyCourseTables(ChoosedCourse choosedCourse) {
        // 先进行判断，只有选课成功且没有过期的课程才能添加
        String status = choosedCourse.getStatus();
        if (!"701001".equals(status)) {
            throw new BusinessException("选课未成功,无法添加到课程表");
        }

        String userId = choosedCourse.getUserId();
        Long courseId = choosedCourse.getCourseId();

        // 因为有主键约束，所以需要先查询是否已经存在，没有再执行插入
        OwnedCourse ownedCourse = getOwnedCourse(userId, courseId);
        if (ownedCourse != null) return ownedCourse;


        OwnedCourse ownedCourseNew = new OwnedCourse();
        BeanUtils.copyProperties(choosedCourse, ownedCourseNew);
        // 补全字段
        ownedCourseNew.setChooseCourseId(choosedCourse.getId());    // 选课
        ownedCourseNew.setCourseType(choosedCourse.getOrderType()); // 选课类型
        ownedCourseNew.setUpdateDate(LocalDateTime.now());

        return ownedCourseNew;


    }

    private OwnedCourse getOwnedCourse(String userId, Long courseId) {
        return ownedCourseMapper.
                selectOne(new LambdaQueryWrapper<OwnedCourse>()
                        .eq(OwnedCourse::getUserId, userId)
                        .eq(OwnedCourse::getCourseId, courseId));
    }


    /**
     * @author CalmKin
     * @description 根据选课id,将课程添加到我的课程表
     * @version 1.0
     * @date 2024/2/5 10:09
     */
    @Override
    @Transactional
    public boolean saveChooseCourseStauts(String courseId) {

        // 先看看这门课在不在选课记录表里面
        ChoosedCourse choosedCourse = chooseCourseMapper.selectById(courseId);
        if(choosedCourse == null)
        {
            log.debug("根据选课id找不到选课记录,选课id:{}",courseId);
            return false;
        }

        // 更改选课状态，幂等性判断
        String status = choosedCourse.getStatus();

        // 只有状态为未支付的时候,才需要将状态更改为已支付
        if("701002".equals(status))
        {
            choosedCourse.setStatus("701001");
            int update = chooseCourseMapper.updateById(choosedCourse);
            if(update <= 0)
            {
                log.debug("更新选课状态失败: {}", choosedCourse);
                return false;
            }
        }

        // 添加到我的选课表
        OwnedCourse ownedCourse = addMyCourseTables(choosedCourse);

        return true;
    }


}
