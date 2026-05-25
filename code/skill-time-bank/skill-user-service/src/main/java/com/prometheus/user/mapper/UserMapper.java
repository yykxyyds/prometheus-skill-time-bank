package com.prometheus.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prometheus.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT COUNT(1) FROM skill_order WHERE (buyer_id = #{userId} OR seller_id = #{userId}) AND status = 4")
    Long countCompletedOrders(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM skill WHERE user_id = #{userId} AND status = 1")
    Long countPublishedSkills(@Param("userId") Long userId);

    @Select("SELECT * FROM user WHERE id = #{id} FOR UPDATE")
    com.prometheus.user.entity.User selectByIdForUpdate(@Param("id") Long id);
}
