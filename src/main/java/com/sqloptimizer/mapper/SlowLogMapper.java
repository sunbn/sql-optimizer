package com.sqloptimizer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqloptimizer.entity.SlowLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 慢查询日志Mapper
 */
@Mapper
public interface SlowLogMapper extends BaseMapper<SlowLog> {
}