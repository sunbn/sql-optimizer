package com.sqloptimizer.core.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 成本评估器
 */
@Slf4j
@Component
public class CostEvaluator {

    /** 基础分数 */
    private static final int BASE_SCORE = 100;

    /** 全表扫描扣分 */
    private static final int FULL_TABLE_SCAN_PENALTY = 30;

    /** 未使用索引扣分 */
    private static final int NO_INDEX_PENALTY = 25;

    /** 文件排序扣分 */
    private static final int FILESORT_PENALTY = 15;

    /** 临时表扣分 */
    private static final int TEMPORARY_TABLE_PENALTY = 10;

    /** 大偏移量扣分 */
    private static final int LARGE_OFFSET_PENALTY = 10;

    /**
     * 评估SQL执行计划得分
     */
    public int evaluateScore(List<Map<String, Object>> explainResult) {
        if (explainResult == null || explainResult.isEmpty()) {
            return BASE_SCORE;
        }

        int score = BASE_SCORE;

        for (Map<String, Object> row : explainResult) {
            // 检查全表扫描
            if (isFullTableScan(row)) {
                score -= FULL_TABLE_SCAN_PENALTY;
            }

            // 检查是否使用索引
            if (!isUsingIndex(row)) {
                score -= NO_INDEX_PENALTY;
            }

            // 检查文件排序
            if (hasFilesort(row)) {
                score -= FILESORT_PENALTY;
            }

            // 检查临时表
            if (hasTemporaryTable(row)) {
                score -= TEMPORARY_TABLE_PENALTY;
            }
        }

        return Math.max(0, score);
    }

    /**
     * 评估SQL复杂度
     */
    public int evaluateComplexity(String sql) {
        int complexity = 0;

        // 检查JOIN数量
        int joinCount = countJoins(sql);
        complexity += joinCount * 5;

        // 检查子查询
        if (sql.toLowerCase().contains("select") && 
            sql.toLowerCase().indexOf("select") != sql.toLowerCase().lastIndexOf("select")) {
            complexity += 10;
        }

        // 检查UNION
        if (sql.toLowerCase().contains("union")) {
            complexity += 5;
        }

        // 检查GROUP BY
        if (sql.toLowerCase().contains("group by")) {
            complexity += 5;
        }

        // 检查ORDER BY
        if (sql.toLowerCase().contains("order by")) {
            complexity += 3;
        }

        return complexity;
    }

    /**
     * 检查是否全表扫描
     */
    private boolean isFullTableScan(Map<String, Object> explainRow) {
        Object type = explainRow.get("type");
        return "ALL".equals(type);
    }

    /**
     * 检查是否使用索引
     */
    private boolean isUsingIndex(Map<String, Object> explainRow) {
        Object key = explainRow.get("key");
        return key != null && !key.toString().isEmpty() && !"NULL".equals(key.toString());
    }

    /**
     * 检查是否有文件排序
     */
    private boolean hasFilesort(Map<String, Object> explainRow) {
        Object extra = explainRow.get("Extra");
        if (extra != null) {
            return extra.toString().contains("Using filesort");
        }
        return false;
    }

    /**
     * 检查是否使用临时表
     */
    private boolean hasTemporaryTable(Map<String, Object> explainRow) {
        Object extra = explainRow.get("Extra");
        if (extra != null) {
            return extra.toString().contains("Using temporary");
        }
        return false;
    }

    /**
     * 统计JOIN数量
     */
    private int countJoins(String sql) {
        String lowerSql = sql.toLowerCase();
        int count = 0;
        int index = 0;
        while ((index = lowerSql.indexOf("join", index)) != -1) {
            count++;
            index += 4;
        }
        return count;
    }
}