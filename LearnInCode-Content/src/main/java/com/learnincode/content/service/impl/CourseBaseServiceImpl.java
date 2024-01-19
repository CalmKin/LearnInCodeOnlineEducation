package com.learnincode.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.base.model.PageParams;
import com.learnincode.base.model.PageResult;
import com.learnincode.content.mapper.CourseBaseMapper;
import com.learnincode.content.mapper.CourseCategoryMapper;
import com.learnincode.content.mapper.CourseMarketMapper;
import com.learnincode.content.model.dto.AddCourseDto;
import com.learnincode.content.model.dto.CourseBaseInfoDto;
import com.learnincode.content.model.dto.QueryCourseParamsDto;
import com.learnincode.content.model.dto.UpdateCourseDto;
import com.learnincode.content.model.po.CourseBase;
import com.learnincode.content.model.po.CourseCategory;
import com.learnincode.content.model.po.CourseMarket;
import com.learnincode.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 86158
 * @description 针对表【course_base(课程基本信息)】的数据库操作Service实现
 * @createDate 2024-01-17 11:30:41
 */
@Service
@Slf4j
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase>
        implements CourseBaseService {


    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper categoryMapper;

    /**
     * 课程分页查询接口
     *
     * @param pageParams           分页参数
     * @param queryCourseParamsDto 课程查询条件（课程名称，审核状态，发布状态）
     * @return 分页返回值
     */
    @Override
    public PageResult<CourseBase> pageCourseBase(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {



        // 构造查询条件
        LambdaQueryWrapper<CourseBase> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        lqw.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        lqw.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());


        Long size = pageParams.getPageSize();
        Long offset = pageParams.getPageNo();
        Page<CourseBase> page = new Page<>(offset, size);


        Page<CourseBase> res = page(page, lqw);
        List<CourseBase> records = res.getRecords();
        log.info("分页查询到课程记录为:{}", records);

        long total = res.getTotal();

        return new PageResult<>(records, total, offset, size);
    }

    @Override
    @Transactional
    public CourseBaseInfoDto addCourseBaseInfo(Long companyId, AddCourseDto dto) {
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new BusinessException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new BusinessException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new BusinessException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new BusinessException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new BusinessException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new BusinessException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new BusinessException("收费规则为空");
        }

        // 插入courseBase表
        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(dto, courseBaseNew);
        //设置审核状态（未提交）
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态（未发布）
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0) throw new BusinessException("课程信息插入失败");


        // 插入CourseMarket表
        Long courseId = courseBaseNew.getId();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);
        // 用代理防止事务失效
        CourseBaseServiceImpl proxy = (CourseBaseServiceImpl)AopContext.currentProxy();
        int ret = proxy.insertCourseMarketInfo(courseMarket);
        if (ret <= 0) throw new BusinessException("营销信息插入失败");


        // 再次查表，组装CourseBaseInfoDto
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfoDtoById(courseId);

        return courseBaseInfoDto;
    }


    /**
     * @author CalmKin
     * @description 课程营销信息，如果不存在，执行插入，存在执行更新
     * @version 1.0
     * @date 2024/1/18 19:42
     */
    @Transactional
    public int insertCourseMarketInfo(CourseMarket courseMarketNew) {
        checkCourseMarketInfo(courseMarketNew);

        CourseMarket courseMarket = courseMarketMapper.selectById(courseMarketNew.getId());

        // 不存在，执行插入
        if (courseMarket == null) {
            return courseMarketMapper.insert(courseMarketNew);
        }

        // 存在，执行更新(因为courseMarketNew已经被设置过课程id了，所以可以直接用这个来更新)
        return courseMarketMapper.updateById(courseMarketNew);
    }


    /**
     * @author CalmKin
     * @description 根据基本信息和营销信息，组装出一个完整的课程信息
     * @version 1.0
     * @date 2024/1/18 20:34
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfoDtoById(Long courseId) {

        // 查询基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) return null;
        // 组装基本信息
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);

        // 查询营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 营销信息为空时，调用copyProperties会空指针
        if (courseMarket != null) {
            // 组装营销信息
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        // 根据分类id，查询分类名称
        // 大分类
        CourseCategory mtCategory = categoryMapper.selectById(courseBase.getMt());
        // 小分类
        CourseCategory stCategory = categoryMapper.selectById(courseBase.getSt());

        // 组装分类信息
        courseBaseInfoDto.setMtName(mtCategory.getName());
        courseBaseInfoDto.setStName(stCategory.getName());

        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBaseInfo(Long companyId ,UpdateCourseDto dto) {
        if(companyId < 0 || dto == null) throw new BusinessException("参数有误,无法更新");

        Long courseId = dto.getId();
        // 查询原有课程信息
        CourseBase courseBase = getById(courseId);
        if(courseBase==null){
            throw new BusinessException("课程不存在,无法更新");
        }

        // 查询原有课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        Long _companyId = courseBase.getCompanyId();
        // 必须是这个机构的课程才能修改
        if(!companyId.equals(_companyId)) throw new BusinessException("课程所属机构和当前机构不一致,无法修改");


        // 属性拷贝
        BeanUtils.copyProperties(dto, courseBase);
        updateById(courseBase);

        BeanUtils.copyProperties(dto, courseMarket);
        // 更新之前先校验营销信息
        checkCourseMarketInfo(courseMarket);
        courseMarketMapper.updateById(courseMarket);


        return getCourseBaseInfoDtoById(courseId);
    }



    /**
     * @author CalmKin
     * @description 校验课程营销信息
     * @version 1.0
     * @date 2024/1/19 11:01
     */
    private void checkCourseMarketInfo(CourseMarket courseMarketNew) {
        // 参数合法性校验
        //收费规则
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isBlank(charge)) {
            throw new BusinessException("收费规则没有选择");
        }
        //收费规则为收费
        if (charge.equals("201001")) {
            if (courseMarketNew.getPrice() == null || courseMarketNew.getOriginalPrice()<=0 || courseMarketNew.getPrice().floatValue() <= 0 ) {
                throw new BusinessException("课程为收费价格不能为空且必须大于0");
            }
        }
    }

}




