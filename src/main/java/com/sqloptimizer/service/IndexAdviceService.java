package com.sqloptimizer.service;

import com.sqloptimizer.core.advisor.IndexAdvisor;
import com.sqloptimizer.core.advisor.RuleEngine;
import com.sqloptimizer.dto.SqlAnalyzeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 索引建议服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexAdviceService {

    private final IndexAdvisor indexAdvisor;
    private final RuleEngine ruleEngine;

    /**
     * 生成索引建议
     */
    public List<SqlAnalyzeResult.IndexSuggestion> generateIndexAdvice(String sql, List<Map<String, Object>> explainResult) {
        log.info("生成索引建议: {}", sql);
        return indexAdvisor.generateIndexAdvice(sql, explainResult);
    }

    /**
     * 执行优化规则检查
     */
    public List<SqlAnalyzeResult.OptimizationTip> checkOptimizationRules(String sql, List<Map<String, Object>> explainResult) {
        log.info("执行优化规则检查: {}", sql);
        return ruleEngine.checkRules(sql, explainResult);
    }
}