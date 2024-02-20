package com.learnincode.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.content.mapper.TeachplanMapper;
import com.learnincode.content.mapper.TeachplanMediaMapper;
import com.learnincode.content.model.dto.BindTeachplanMediaDto;
import com.learnincode.content.model.dto.SaveTeachplanDto;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.model.po.Teachplan;
import com.learnincode.content.model.po.TeachplanMedia;
import com.learnincode.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author 86158
* @description 针对表【teachplan(课程计划)】的数据库操作Service实现
* @createDate 2024-01-19 19:02:18
*/
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan>
    implements TeachplanService{

    @Autowired
    private TeachplanMapper teachplanMapper;


    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    /**
     * @author CalmKin
     * @description 查询课程计划树
     * @version 1.0
     * @date 2024/1/19 20:32
     */
    @Override
    public List<TeachplanDto> getTeachPlanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }



    @Override
    @Transactional
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {

        // 先获取课程id，根据是否有课程id来判断操作
        Long id = teachplanDto.getId();
        // 有id，说明是更新操作
        if( id != null)
        {
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            updateById(teachplan);
        }
        // 否则，说明是新增操作
        else
        {
            // 因为要给新增的课程添加order属性，所以要先查出下一个分配的order是多少
            // orderNew = max(orderCur) + 1
            LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();


            Long courseId = teachplanDto.getCourseId();
            Long parentid = teachplanDto.getParentid();
            // 当前课程下，父章节的最大order是多少
            Integer nxtOrder = teachplanMapper.selectMaxOrderby(parentid,courseId) + 1;

            Teachplan teachplanNew = new Teachplan();
            BeanUtils.copyProperties(teachplanDto, teachplanNew);
            teachplanNew.setOrderby(nxtOrder);

            save(teachplanNew);
        }

    }

    @Override
    @Transactional
    public void deleteTeachplan(Long teachplanId) {
        // 根据id查询课程计划
        Teachplan teachplan = getById(teachplanId);
        Integer grade = teachplan.getGrade();

        // 如果是一级章节
        if(grade == 1)
        {
            // 检查是否还有二级章节
            LambdaQueryWrapper<Teachplan> lqw  =new LambdaQueryWrapper<>();
            lqw.eq(Teachplan::getParentid, teachplanId);
            List<Teachplan> list = list(lqw);

            // 可以直接删除
            if(list == null || list.size() == 0)
            {
                removeById(teachplanId);
            }
            // 如果还有二级课程，那么不能删除
            else
            {
                throw new BusinessException("课程计划信息还有子级信息，无法操作");
            }
        }
        // 如果是第二章节，直接删除
        else
        {
            // 先删除课程计划
            removeById(teachplanId);
            // 再删除媒资关联表里面的信息
            LambdaQueryWrapper<TeachplanMedia> lqw = new LambdaQueryWrapper<>();
            lqw.eq(TeachplanMedia::getTeachplanId, teachplanId);
            teachplanMediaMapper.delete(lqw);

            //TODO 删除媒资信息？
        }
    }

    @Override
    @Transactional
    public void movedownTeachplan(Long teachPlanId) {

        Teachplan teachplan = getById(teachPlanId);
        // 找到下一个课程计划
       Teachplan nextPlan = teachplanMapper.getNextPlan(teachPlanId);
        // 没有下一个计划，不能下移
        if(nextPlan == null) return;

        // 交换两个课程的orderBy
        Integer tmpOrderby = teachplan.getOrderby();
        teachplan.setOrderby(nextPlan.getOrderby());
        nextPlan.setOrderby(tmpOrderby);

        // 更新两个课程的信息
        updateById(teachplan);
        updateById(nextPlan);
    }

    @Override
    @Transactional
    public void moveupTeachplan(Long teachPlanId) {
        Teachplan teachplan = getById(teachPlanId);
        // 找到前一个课程计划
        Teachplan prePlan = teachplanMapper.getPrePlan(teachPlanId);
        // 没有前一个计划，不能上移
        if(prePlan == null) return;

        // 交换两个课程的orderBy
        Integer tmpOrderby = teachplan.getOrderby();
        teachplan.setOrderby(prePlan.getOrderby());
        prePlan.setOrderby(tmpOrderby);

        // 更新两个课程的信息
        updateById(teachplan);
        updateById(prePlan);
    }

    /**
     * 将视频文件和教学计划关联
     * @param bindTeachplanMediaDto
     */
    @Override
    @Transactional
    public void attachMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        // 根据教学计划查找对应课程Id
        Teachplan teachplan = teachplanMapper.selectOne(new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getId, bindTeachplanMediaDto
                .getTeachplanId()));
        if(teachplan == null)
        {
            throw new BusinessException("教学计划不存在");
        }

        Integer grade = teachplan.getGrade();
        if(grade!=2){
            throw new BusinessException("只允许第二级教学计划绑定媒资文件");
        }

        Long courseId = teachplan.getCourseId();

        // 媒资和课程计划绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);
        teachplanMedia.setCourseId(courseId);

        // 先删除原来的绑定关系
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId,bindTeachplanMediaDto.getTeachplanId()));

        // 再添加新的绑定关系
        teachplanMediaMapper.insert(teachplanMedia);

    }





}




