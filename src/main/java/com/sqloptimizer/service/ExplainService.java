package com.sqloptimizer.service;

import com.sqloptimizer.core.analyzer.CostEvaluator;
import com.sqloptimizer.core.analyzer.ExplainAnalyzer;
import com.sqloptimizer.dto.SqlAnalyzeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * EXPLAIN分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExplainService {

    private final ExplainAnalyzer explainAnalyzer;
    private final CostEvaluator costEvaluator;

    /**
     * 执行EXPLAIN分析
     */
    public List<Map<String, Object>> analyze(DataSource dataSource, String sql) {
        log.info("执行EXPLAIN分析: {}", sql);
        return explainAnalyzer.analyze(dataSource, sql);
    }

    /**
     * 执行EXPLAIN JSON分析
     */
    public String analyzeJson(DataSource dataSource, String sql) {
        log.info("执行EXPLAIN JSON分析: {}", sql);
        return explainAnalyzer.analyzeJson(dataSource, sql);
    }

    /**
     * 评估SQL得分
     */
    public int evaluateScore(List<Map<String, Object>> explainResult) {
        return costEvaluator.evaluateScore(explainResult);
    }

    /**
     * 评估SQL复杂度
     */
    public int evaluateComplexity(String sql) {
        return costEvaluator.evaluateComplexity(sql);
    }

    /**
     * 检查是否使用索引
     */
    public boolean isUsingIndex(Map<String, Object> explainRow) {
        return explainAnalyzer.isUsingIndex(explainRow);
    }

    /**
     * 检查是否全表扫描
     */
    public boolean isFullTableScan(Map<String, Object> explainRow) {
        return explainAnalyzer.isFullTableScan(explainRow);
    }
}