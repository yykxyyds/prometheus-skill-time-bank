package com.prometheus.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.skill.entity.Bounty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 悬赏 Mapper
 */
@Mapper
public interface BountyMapper extends BaseMapper<Bounty> {

    /**
     * 分页查询悬赏（联表查用户名）
     */
    @Select("<script>" +
            "SELECT b.*, u.username AS user_name FROM bounty b " +
            "LEFT JOIN user u ON b.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='status != null'>AND b.status = #{status}</if>" +
            "ORDER BY b.create_time DESC" +
            "</script>")
    Page<Bounty> selectPageWithUser(Page<Bounty> page, @Param("status") Integer status);

    @Select("SELECT b.*, u.username AS user_name FROM bounty b " +
            "LEFT JOIN user u ON b.user_id = u.id " +
            "WHERE b.id = #{id}")
    Bounty selectByIdWithUser(@Param("id") Long id);
}
