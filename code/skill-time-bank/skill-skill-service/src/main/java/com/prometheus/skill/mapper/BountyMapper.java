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
     * 分页查询悬赏（联表查用户名和分类名）
     *
     * @param keyword    关键词搜索（标题/描述）
     * @param categoryId 分类ID筛选（null 或 0 表示不筛选）
     * @param type       筛选类型：null-仅已发布, "publish"-我发布的, "take"-我接的, "complete"-我完成的, "all"-全部
     * @param userId     当前登录用户 ID（type 不为 null 时必填）
     */
    @Select("<script>" +
            "SELECT b.*, c.name AS category_name, u.username AS user_name, u.avatar AS user_avatar, " +
            "au.username AS applicant_name, au.avatar AS applicant_avatar FROM bounty b " +
            "LEFT JOIN skill_category c ON b.category_id = c.id " +
            "LEFT JOIN user u ON b.user_id = u.id " +
            "LEFT JOIN user au ON b.applicant_id = au.id " +
            "WHERE 1=1 " +
            "<choose>" +
            "  <when test='type == \"all\" or type == \"publish\" or type == \"take\" or type == \"complete\" or type == \"apply\"'>" +
            "    <if test='type == \"publish\"'>AND b.user_id = #{userId}</if>" +
            "    <if test='type == \"take\"'>AND b.applicant_id = #{userId}</if>" +
            "    <if test='type == \"complete\"'>AND b.status = 3 AND (b.user_id = #{userId} OR b.applicant_id = #{userId})</if>" +
            "    <if test='type == \"apply\"'>AND b.id IN (SELECT ba.bounty_id FROM bounty_application ba WHERE ba.applicant_id = #{userId} AND ba.status = 1)</if>" +
            "  </when>" +
            "  <otherwise>AND b.status = 1</otherwise>" +
            "</choose>" +
            "<if test='status != null'>AND b.status = #{status}</if>" +
            "<if test='categoryId != null and categoryId != 0'>AND b.category_id = #{categoryId}</if>" +
            "<if test='keyword != null and keyword != \"\"'>AND (b.title LIKE CONCAT('%',#{keyword},'%') OR b.description LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "ORDER BY b.create_time DESC" +
            "</script>")
    Page<Bounty> selectPageWithUser(Page<Bounty> page, @Param("status") Integer status,
                                   @Param("keyword") String keyword,
                                   @Param("categoryId") Long categoryId,
                                   @Param("type") String type, @Param("userId") Long userId);

    @Select("SELECT b.*, c.name AS category_name, u.username AS user_name, u.avatar AS user_avatar, " +
            "au.username AS applicant_name, au.avatar AS applicant_avatar FROM bounty b " +
            "LEFT JOIN skill_category c ON b.category_id = c.id " +
            "LEFT JOIN user u ON b.user_id = u.id " +
            "LEFT JOIN user au ON b.applicant_id = au.id " +
            "WHERE b.id = #{id}")
    Bounty selectByIdWithUser(@Param("id") Long id);
}
