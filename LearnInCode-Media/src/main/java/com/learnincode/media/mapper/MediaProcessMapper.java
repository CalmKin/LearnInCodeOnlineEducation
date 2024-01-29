package com.learnincode.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learnincode.media.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /**
     * @param executorId 执行器id
     * @param totalExecutor 执行器总数
     * @param taskCnt 单个执行器需要执行的任务数
     * @return
     */
    @Select("select * from xcplus_media.media_process t where t.id%#{totalExecutor} = #{executorId} and ( t.status = '1' or (t.status = '3' and t.fail_count < 3 ) ) limit #{taskCnt}")
    List<MediaProcess> getTaskList(@Param("executorId") int executorId,@Param("totalExecutor") int totalExecutor,@Param("taskCnt") int taskCnt);

    /**
     * 利用数据库实现乐观锁,执行任务前先获取乐观锁
     * @param id
     * @return
     */
    @Update("update xcplus_media.media_process t set t.status = 4 where t.id = #{id} and (t.status = '1' or (t.status = '3' and t.fail_count < 3 ) )")
    int tryLock(@Param("id") long id);
}
