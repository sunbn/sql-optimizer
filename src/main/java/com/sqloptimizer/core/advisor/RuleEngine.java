package com.sqloptimizer.core.advisor;

import com.sqloptimizer.dto.SqlAnalyzeResult;
import com.sqloptimizer.entity.OptimizationRule;
import com.sqloptimizer.service.OptimizationRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SQL优化规则引擎
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEngine {

    private final OptimizationRuleService optimizationRuleService;

    /**
     * 执行规则检查
     */
    public List<SqlAnalyzeResult.OptimizationTip> checkRules(String sql, List<Map<String, Object>> explainResult) {
        List<SqlAnalyzeResult.OptimizationTip> tips = new ArrayList<>();

        // 检查SELECT *
        checkSelectStar(sql, tips);

        // 检查LIKE前缀模糊查询
        checkLikePrefix(sql, tips);

        // 检查OR条件
        checkOrCondition(sql, tips);

        // 检查子查询
        checkSubquery(sql, tips);

        // 检查LIMIT大偏移量
        checkLimitOffset(sql, tips);

        // 检查IN子句
        checkInClause(sql, tips);

        // 检查隐式类型转换
        checkImplicitConversion(sql, tips);

        // 基于EXPLAIN结果检查
        if (explainResult != null) {
            checkExplainResult(explainResult, tips);
        }

        // 按优先级排序
        tips.sort((a, b) -> b.getPriority().compareTo(a.getPriority()));

        return tips;
    }

    /**
     * 检查SELECT *
     */
    private void checkSelectStar(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_SELECT_STAR")) {
            return;
        }
        if (Pattern.compile("(?i)SELECT\\s+\\*\\s+FROM").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_SELECT_STAR", 8);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查LIKE前缀模糊查询
     */
    private void checkLikePrefix(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_LIKE_PREFIX")) {
            return;
        }
        if (Pattern.compile("(?i)LIKE\\s+['\"]%").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_LIKE_PREFIX", 7);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查OR条件
     */
    private void checkOrCondition(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_OR_CONDITION")) {
            return;
        }
        if (Pattern.compile("(?i)\\bOR\\b").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_OR_CONDITION", 6);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查子查询
     */
    private void checkSubquery(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_SUBQUERY")) {
            return;
        }
        String lowerSql = sql.toLowerCase();
        int firstSelect = lowerSql.indexOf("select");
        int lastSelect = lowerSql.lastIndexOf("select");
        if (firstSelect != lastSelect) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_SUBQUERY", 6);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查LIMIT大偏移量
     */
    private void checkLimitOffset(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_LIMIT_OFFSET")) {
            return;
        }
        java.util.regex.Matcher matcher = Pattern.compile("(?i)LIMIT\\s+(\\d+)\\s*,\\s*(\\d+)").matcher(sql);
        if (matcher.find()) {
            int offset = Integer.parseInt(matcher.group(1));
            if (offset > 10000) {
                SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_LIMIT_OFFSET", 7);
                if (tip != null) tips.add(tip);
            }
        }
    }

    /**
     * 检查IN子句
     */
    private void checkInClause(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_IN_CLAUSE")) {
            return;
        }
        java.util.regex.Matcher matcher = Pattern.compile("(?i)IN\\s*\\(([^)]+)\\)").matcher(sql);
        if (matcher.find()) {
            String inContent = matcher.group(1);
            int count = 1;
            for (int i = 0; i < inContent.length(); i++) {
                if (inContent.charAt(i) == ',') {
                    count++;
                }
            }
            if (count > 100) {
                SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_IN_CLAUSE", 5);
                if (tip != null) tips.add(tip);
            }
        }
    }

    /**
     * 检查隐式类型转换
     */
    private void checkImplicitConversion(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_IMPLICIT_CONVERSION")) {
            return;
        }
        if (Pattern.compile("(?i)[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*['\"]\\d+['\"]").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_IMPLICIT_CONVERSION", 8);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 基于EXPLAIN结果检查
     */
    private void checkExplainResult(List<Map<String, Object>> explainResult, List<SqlAnalyzeResult.OptimizationTip> tips) {
        for (Map<String, Object> row : explainResult) {
            // 检查全表扫描
            if (optimizationRuleService.isEnabled("RULE_FULL_TABLE_SCAN")) {
                Object type = row.get("type");
                if ("ALL".equals(type)) {
                    SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_FULL_TABLE_SCAN", 9);
                    if (tip != null) tips.add(tip);
                }
            }

            // 检查未使用索引
            if (optimizationRuleService.isEnabled("RULE_MISSING_INDEX")) {
                Object key = row.get("key");
                if (key == null || key.toString().isEmpty() || "NULL".equals(key.toString())) {
                    boolean hasMissingIndex = tips.stream()
                            .anyMatch(t -> "RULE_MISSING_INDEX".equals(t.getRuleCode()));
                    if (!hasMissingIndex) {
                        SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_MISSING_INDEX", 10);
                        if (tip != null) tips.add(tip);
                    }
                }
            }
        }
    }

    /**
     * 从数据库创建优化提示
     */
    private SqlAnalyzeResult.OptimizationTip createTip(String ruleCode, int defaultPriority) {
        OptimizationRule rule = optimizationRuleService.getRule(ruleCode);
        if (rule == null) {
            return null;
        }
        SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
        tip.setRuleCode(rule.getRuleCode());
        tip.setRuleName(rule.getRuleName());
        tip.setDescription(rule.getDescription());
        tip.setSuggestion(rule.getSuggestion());
        tip.setPriority(optimizationRuleService.getPriority(ruleCode, defaultPriority));
        return tip;
    }
}
