package com.sqloptimizer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * SQL分析请求DTO
 */
@Data
public class SqlAnalyzeRequest {

    /** 数据源ID */
    private Long dataSourceId;

    /** SQL内容 */
    @NotBlank(message = "SQL内容不能为空")
    private String sqlContent;

    /** 是否执行EXPLAIN分析 */
    private Boolean executeExplain = true;

    /** 是否生成索引建议 */
    private Boolean generateIndexAdvice = true;

    /** 是否执行优化规则检查 */
    private Boolean checkOptimizationRules = true;
}