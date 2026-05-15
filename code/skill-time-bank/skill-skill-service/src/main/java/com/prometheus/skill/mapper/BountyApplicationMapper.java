package com.prometheus.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prometheus.skill.entity.BountyApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 悬赏申请 Mapper
 */
@Mapper
public interface BountyApplicationMapper extends BaseMapper<BountyApplication> {
}
