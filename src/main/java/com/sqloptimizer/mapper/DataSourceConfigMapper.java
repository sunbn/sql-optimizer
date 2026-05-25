package com.sqloptimizer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqloptimizer.entity.DataSourceConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源配置Mapper
 */
@Mapper
public interface DataSourceConfigMapper extends BaseMapper<DataSourceConfig> {
}