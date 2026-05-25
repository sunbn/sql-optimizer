package com.sqloptimizer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqloptimizer.entity.SqlAnalysisRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * SQL分析记录Mapper
 */
@Mapper
public interface SqlAnalysisRecordMapper extends BaseMapper<SqlAnalysisRecord> {
}