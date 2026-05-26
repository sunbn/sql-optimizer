package com.sqloptimizer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqloptimizer.entity.OptimizationRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优化规则Mapper
 */
@Mapper
public interface OptimizationRuleMapper extends BaseMapper<OptimizationRule> {
}