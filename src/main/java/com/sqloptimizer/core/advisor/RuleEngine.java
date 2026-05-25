package com.sqloptimizer.core.advisor;

import com.sqloptimizer.dto.SqlAnalyzeResult;
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
public class RuleEngine {

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
        if (Pattern.compile("(?i)SELECT\\s+\\*\\s+FROM").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
            tip.setRuleCode("RULE_SELECT_STAR");
            tip.setRuleName("避免SELECT *");
            tip.setDescription("使用SELECT *会返回所有列，增加网络传输和内存消耗");
            tip.setSuggestion("明确指定需要的列，避免返回不必要的数据");
            tip.setPriority(8);
            tips.add(tip);
        }
    }

    /**
     * 检查LIKE前缀模糊查询
     */
    private void checkLikePrefix(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (Pattern.compile("(?i)LIKE\\s+['\"]%").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
            tip.setRuleCode("RULE_LIKE_PREFIX");
            tip.setRuleName("LIKE前缀模糊查询");
            tip.setDescription("LIKE以%开头会导致索引失效");
            tip.setSuggestion("考虑使用全文索引或优化查询方式");
            tip.setPriority(7);
            tips.add(tip);
        }
    }

    /**
     * 检查OR条件
     */
    private void checkOrCondition(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        // 简单检查OR条件
        if (Pattern.compile("(?i)\\bOR\\b").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
            tip.setRuleCode("RULE_OR_CONDITION");
            tip.setRuleName("OR条件优化");
            tip.setDescription("OR条件可能导致索引失效");
            tip.setSuggestion("考虑使用UNION ALL替代OR");
            tip.setPriority(6);
            tips.add(tip);
        }
    }

    /**
     * 检查子查询
     */
    private void checkSubquery(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        String lowerSql = sql.toLowerCase();
        int firstSelect = lowerSql.indexOf("select");
        int lastSelect = lowerSql.lastIndexOf("select");
        if (firstSelect != lastSelect) {
            SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
            tip.setRuleCode("RULE_SUBQUERY");
            tip.setRuleName("子查询优化");
            tip.setDescription("子查询可能导致性能问题");
            tip.setSuggestion("考虑使用JOIN替代子查询");
            tip.setPriority(6);
            tips.add(tip);
        }
    }

    /**
     * 检查LIMIT大偏移量
     */
    private void checkLimitOffset(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        java.util.regex.Matcher matcher = Pattern.compile("(?i)LIMIT\\s+(\\d+)\\s*,\\s*(\\d+)").matcher(sql);
        if (matcher.find()) {
            int offset = Integer.parseInt(matcher.group(1));
            if (offset > 10000) {
                SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
                tip.setRuleCode("RULE_LIMIT_OFFSET");
                tip.setRuleName("LIMIT大偏移量");
                tip.setDescription("LIMIT大偏移量会导致扫描大量数据");
                tip.setSuggestion("使用覆盖索引或优化分页方式");
                tip.setPriority(7);
                tips.add(tip);
            }
        }
    }

    /**
     * 检查IN子句
     */
    private void checkInClause(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
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
                SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
                tip.setRuleCode("RULE_IN_CLAUSE");
                tip.setRuleName("IN子句数量过多");
                tip.setDescription("IN子句中元素过多影响性能");
                tip.setSuggestion("考虑使用临时表或分批处理");
                tip.setPriority(5);
                tips.add(tip);
            }
        }
    }

    /**
     * 检查隐式类型转换
     */
    private void checkImplicitConversion(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        // 检查数字与字符串比较
        if (Pattern.compile("(?i)[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*['\"]\\d+['\"]").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
            tip.setRuleCode("RULE_IMPLICIT_CONVERSION");
            tip.setRuleName("隐式类型转换");
            tip.setDescription("WHERE条件中存在隐式类型转换，导致索引失效");
            tip.setSuggestion("确保比较双方类型一致");
            tip.setPriority(8);
            tips.add(tip);
        }
    }

    /**
     * 基于EXPLAIN结果检查
     */
    private void checkExplainResult(List<Map<String, Object>> explainResult, List<SqlAnalyzeResult.OptimizationTip> tips) {
        for (Map<String, Object> row : explainResult) {
            // 检查全表扫描
            Object type = row.get("type");
            if ("ALL".equals(type)) {
                SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
                tip.setRuleCode("RULE_FULL_TABLE_SCAN");
                tip.setRuleName("全表扫描");
                tip.setDescription("SQL执行计划中出现全表扫描");
                tip.setSuggestion("优化查询条件或添加合适的索引");
                tip.setPriority(9);
                tips.add(tip);
            }

            // 检查未使用索引
            Object key = row.get("key");
            if (key == null || key.toString().isEmpty() || "NULL".equals(key.toString())) {
                // 避免重复添加
                boolean hasMissingIndex = tips.stream()
                        .anyMatch(t -> "RULE_MISSING_INDEX".equals(t.getRuleCode()));
                if (!hasMissingIndex) {
                    SqlAnalyzeResult.OptimizationTip tip = new SqlAnalyzeResult.OptimizationTip();
                    tip.setRuleCode("RULE_MISSING_INDEX");
                    tip.setRuleName("缺少索引");
                    tip.setDescription("WHERE条件中的列缺少索引，导致全表扫描");
                    tip.setSuggestion("为WHERE条件中的列添加索引");
                    tip.setPriority(10);
                    tips.add(tip);
                }
            }
        }
    }
}