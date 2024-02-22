package com.learnincode.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learnincode.ucenter.model.po.Menu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * @author CalmKin
     * @description 根据用户id查询用户的权限列表
     * @version 1.0
     * @date 2024/2/22 21:40
     */
    @Select("SELECT	* FROM xc_menu WHERE id IN (SELECT menu_id FROM xc_permission WHERE role_id IN ( SELECT role_id FROM xc_user_role WHERE user_id = #{userId} ))")
    List<Menu> selectPermissionByUserId(@Param("userId") String userId);
}
