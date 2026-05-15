package com.prometheus.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prometheus.wallet.entity.TimeTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimeTransactionMapper extends BaseMapper<TimeTransaction> {
}
