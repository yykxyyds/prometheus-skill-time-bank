package com.prometheus.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.skill.entity.Skill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 技能 Mapper
 */
@Mapper
public interface SkillMapper extends BaseMapper<Skill> {

    /**
     * 分页查询上架技能（联表查分类名称）
     */
    @Select("<script>" +
            "SELECT s.*, c.name AS category_name FROM skill s " +
            "LEFT JOIN skill_category c ON s.category_id = c.id " +
            "WHERE s.status = 1 " +
            "<if test='categoryId != null and categoryId != 0'>AND s.category_id = #{categoryId}</if>" +
            "<if test='keyword != null and keyword != \"\"'>AND s.title LIKE CONCAT('%',#{keyword},'%')</if>" +
            "<choose>" +
            "  <when test='sort == \"price\"'>ORDER BY s.price ASC</when>" +
            "  <when test='sort == \"popular\"'>ORDER BY s.view_count DESC</when>" +
            "  <when test='sort == \"orders\"'>ORDER BY s.order_count DESC</when>" +
            "  <otherwise>ORDER BY s.create_time DESC</otherwise>" +
            "</choose>" +
            "</script>")
    Page<Skill> selectPageWithUser(Page<Skill> page,
                                   @Param("categoryId") Long categoryId,
                                   @Param("keyword") String keyword,
                                   @Param("sort") String sort);
}
