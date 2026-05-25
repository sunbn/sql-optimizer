package com.sqloptimizer.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * SQL分析结果DTO
 */
@Data
public class SqlAnalyzeResult {

    /** 分析记录ID */
    private Long recordId;

    /** SQL类型 */
    private String sqlType;

    /** 涉及的表 */
    private List<String> tables;

    /** SQL评分(0-100) */
    private Integer score;

    /** 执行计划结果 */
    private List<Map<String, Object>> explainResult;

    /** 索引建议列表 */
    private List<IndexSuggestion> indexSuggestions;

    /** 优化提示列表 */
    private List<OptimizationTip> optimizationTips;

    /** 分析状态 */
    private String status;

    /** 错误信息 */
    private String errorMsg;

    /**
     * 索引建议内部类
     */
    @Data
    public static class IndexSuggestion {
        private String tableName;
        private String indexColumns;
        private String indexType;
        private String reason;
        private String estimatedImprovement;
    }

    /**
     * 优化提示内部类
     */
    @Data
    public static class OptimizationTip {
        private String ruleCode;
        private String ruleName;
        private String description;
        private String suggestion;
        private Integer priority;
    }
}