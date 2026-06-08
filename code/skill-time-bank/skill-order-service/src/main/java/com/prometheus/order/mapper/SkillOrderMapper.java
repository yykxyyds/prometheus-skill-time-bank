package com.prometheus.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prometheus.order.entity.SkillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SkillOrderMapper extends BaseMapper<SkillOrder> {

    @Select("SELECT o.*, " +
            "bu.username AS buyer_name, bu.avatar AS buyer_avatar, " +
            "su.username AS seller_name, su.avatar AS seller_avatar, " +
            "s.title AS skill_name, " +
            "b.title AS bounty_title " +
            "FROM skill_order o " +
            "LEFT JOIN user bu ON o.buyer_id = bu.id " +
            "LEFT JOIN user su ON o.seller_id = su.id " +
            "LEFT JOIN skill s ON o.skill_id = s.id " +
            "LEFT JOIN bounty b ON o.bounty_id = b.id " +
            "WHERE o.id = #{id}")
    SkillOrder selectByIdWithDetails(@Param("id") Long id);

    @Select("SELECT * FROM skill_order WHERE bounty_id = #{bountyId} ORDER BY id DESC LIMIT 1")
    SkillOrder selectByBountyId(@Param("bountyId") Long bountyId);

    @Select("SELECT * FROM skill_order WHERE id = #{id} FOR UPDATE")
    SkillOrder selectByIdForUpdate(@Param("id") Long id);

    @Select("SELECT * FROM skill_order WHERE bounty_id = #{bountyId} ORDER BY id DESC LIMIT 1 FOR UPDATE")
    SkillOrder selectByBountyIdForUpdate(@Param("bountyId") Long bountyId);

    @Select("SELECT o.*, " +
            "bu.username AS buyer_name, bu.avatar AS buyer_avatar, " +
            "su.username AS seller_name, su.avatar AS seller_avatar, " +
            "s.title AS skill_name, " +
            "b.title AS bounty_title " +
            "FROM skill_order o " +
            "LEFT JOIN user bu ON o.buyer_id = bu.id " +
            "LEFT JOIN user su ON o.seller_id = su.id " +
            "LEFT JOIN skill s ON o.skill_id = s.id " +
            "LEFT JOIN bounty b ON o.bounty_id = b.id " +
            "WHERE o.buyer_id = #{buyerId} " +
            "ORDER BY o.create_time DESC")
    List<SkillOrder> selectListByBuyerId(@Param("buyerId") Long buyerId);

    @Select("SELECT o.*, " +
            "bu.username AS buyer_name, bu.avatar AS buyer_avatar, " +
            "su.username AS seller_name, su.avatar AS seller_avatar, " +
            "s.title AS skill_name, " +
            "b.title AS bounty_title " +
            "FROM skill_order o " +
            "LEFT JOIN user bu ON o.buyer_id = bu.id " +
            "LEFT JOIN user su ON o.seller_id = su.id " +
            "LEFT JOIN skill s ON o.skill_id = s.id " +
            "LEFT JOIN bounty b ON o.bounty_id = b.id " +
            "WHERE o.seller_id = #{sellerId} " +
            "ORDER BY o.create_time DESC")
    List<SkillOrder> selectListBySellerId(@Param("sellerId") Long sellerId);

    @Select("<script>" +
            "SELECT o.*, " +
            "bu.username AS buyer_name, bu.avatar AS buyer_avatar, " +
            "su.username AS seller_name, su.avatar AS seller_avatar, " +
            "s.title AS skill_name, " +
            "b.title AS bounty_title " +
            "FROM skill_order o " +
            "LEFT JOIN user bu ON o.buyer_id = bu.id " +
            "LEFT JOIN user su ON o.seller_id = su.id " +
            "LEFT JOIN skill s ON o.skill_id = s.id " +
            "LEFT JOIN bounty b ON o.bounty_id = b.id " +
            "WHERE o.id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<SkillOrder> selectBatchIdsWithDetails(@Param("ids") List<Long> ids);
}
