package com.learnincode.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.content.mapper.TeachplanMapper;
import com.learnincode.content.model.dto.SaveTeachplanDto;
import com.learnincode.content.model.dto.TeachplanDto;
import com.learnincode.content.model.po.Teachplan;
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
}




