package com.learnincode.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.content.mapper.CourseCategoryMapper;
import com.learnincode.content.model.dto.CourseCategoryTreeDto;
import com.learnincode.content.model.po.CourseCategory;
import com.learnincode.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 86158
* @description 针对表【course_category(课程分类)】的数据库操作Service实现
* @createDate 2024-01-18 09:30:29
*/
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory>
    implements CourseCategoryService{

    @Autowired
    private CourseCategoryMapper categoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String root) {

        // 查询数据库，得到所有结点
        List<CourseCategoryTreeDto> treeDtoList = categoryMapper.selectTreeNodes(root);


        // 将list转map，方便后续子节点根据parentid找到父结点
        // 第一级结点"1"不需要存进去
        Map<String, CourseCategoryTreeDto> dic = treeDtoList.stream().filter(dto -> !root.equals(dto.getId())).collect(Collectors.toMap(
                // key是id，value是结点本身
                key -> key.getId(), value -> value
        ));



        // 最终要返回的list集合（二级分类）
        List<CourseCategoryTreeDto> ret = new ArrayList<>();

        // 遍历list，把每个结点添加到它的父节点list里面
        treeDtoList.stream().filter(dto->!root.equals(dto.getId())).forEach(
                node->{
                    // 如果是二级分类，那么直接添加到返回集合里面
                    if(root.equals(node.getParentid()))
                    {
                        ret.add(node);
                    }
                    // 从dic里面找到父结点
                    CourseCategoryTreeDto parent = dic.get(node.getParentid());

                    // 如果父节点不为空（大于二级分类），那么添加到对应父结点的subList里面
                    if(parent != null)
                    {

                        List<CourseCategoryTreeDto> subNode = parent.getChildrenTreeNodes();
                        if(subNode == null)
                        {
                            parent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        // 把自己添加到父节点
                        parent.getChildrenTreeNodes().add(node);
                    }
                }
        );

        // 返回结果
        return ret;
    }
}




