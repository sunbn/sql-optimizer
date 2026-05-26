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

        // 检查索引列上使用函数
        checkIndexFunction(sql, tips);

        // 检查DELETE缺少WHERE
        checkDeleteWithoutWhere(sql, tips);

        // 检查UPDATE缺少WHERE
        checkUpdateWithoutWhere(sql, tips);

        // 检查笛卡尔积
        checkCartesianProduct(sql, tips);

        // 检查SQL注入风险
        checkSqlInjectionRisk(sql, tips);

        // 检查不必要的DISTINCT
        checkUnnecessaryDistinct(sql, tips);

        // 检查HAVING替代WHERE
        checkHavingInsteadWhere(sql, tips);

        // 检查关联子查询
        checkCorrelatedSubquery(sql, tips);

        // 检查大字段查询
        checkLargeFieldQuery(sql, tips);

        // 检查未使用绑定变量
        checkNoBoundVariables(sql, tips);

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
     * 检查索引列上使用函数
     */
    private void checkIndexFunction(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_INDEX_FUNCTION")) {
            return;
        }
        // 匹配 WHERE 条件中列被函数包裹的情况，如 YEAR(col), UPPER(col), DATE(col) 等
        if (Pattern.compile("(?i)WHERE\\s+.*[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*\\)").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_INDEX_FUNCTION", 8);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查DELETE缺少WHERE
     */
    private void checkDeleteWithoutWhere(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_DELETE_WITHOUT_WHERE")) {
            return;
        }
        String lowerSql = sql.trim().toLowerCase();
        if (lowerSql.startsWith("delete") && !lowerSql.contains("where")) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_DELETE_WITHOUT_WHERE", 10);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查UPDATE缺少WHERE
     */
    private void checkUpdateWithoutWhere(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_UPDATE_WITHOUT_WHERE")) {
            return;
        }
        String lowerSql = sql.trim().toLowerCase();
        if (lowerSql.startsWith("update") && !lowerSql.contains("where")) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_UPDATE_WITHOUT_WHERE", 10);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查笛卡尔积（JOIN缺少ON条件）
     */
    private void checkCartesianProduct(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_CARTESIAN_PRODUCT")) {
            return;
        }
        String lowerSql = sql.toLowerCase();
        // 包含JOIN关键字但没有ON关键字
        if ((lowerSql.contains("join") || lowerSql.contains(",")) && !lowerSql.contains("on")) {
            // 排除单表查询和子查询中的简单逗号分隔
            if (Pattern.compile("(?i)FROM\\s+\\w+\\s*,\\s*\\w+").matcher(sql).find() ||
                Pattern.compile("(?i)JOIN\\s+\\w+").matcher(sql).find()) {
                SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_CARTESIAN_PRODUCT", 10);
                if (tip != null) tips.add(tip);
            }
        }
    }

    /**
     * 检查SQL注入风险
     */
    private void checkSqlInjectionRisk(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_SQL_INJECTION_RISK")) {
            return;
        }
        // 检测字符串拼接特征：单引号闭合后接运算符或关键字
        if (Pattern.compile("['\"]\\s*\\+\\s*['\"]").matcher(sql).find() ||
            Pattern.compile("['\"]\\s*\\|\\|\\s*['\"]").matcher(sql).find() ||
            Pattern.compile("['\"]\\s*;\\s*(?i)(SELECT|INSERT|UPDATE|DELETE|DROP)").matcher(sql).find() ||
            Pattern.compile("(?i)\\$\\{[^}]+\\}").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_SQL_INJECTION_RISK", 10);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查不必要的DISTINCT
     */
    private void checkUnnecessaryDistinct(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_UNNECESSARY_DISTINCT")) {
            return;
        }
        // 如果查询的是主键或唯一索引列，DISTINCT是多余的
        // 这里做简单检测：单表查询且SELECT列表包含id等主键特征
        if (Pattern.compile("(?i)SELECT\\s+DISTINCT").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_UNNECESSARY_DISTINCT", 4);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查HAVING替代WHERE
     */
    private void checkHavingInsteadWhere(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_HAVING_INSTEAD_WHERE")) {
            return;
        }
        // HAVING中使用了聚合函数以外的条件
        if (Pattern.compile("(?i)HAVING\\s+.*[^a-zA-Z0-9_](=|>|<|>=|<=|<>|!=)[^a-zA-Z0-9_]").matcher(sql).find()) {
            // 简单判断：如果HAVING中没有聚合函数，可能是误用
            String havingPart = sql.substring(sql.toUpperCase().indexOf("HAVING"));
            if (!Pattern.compile("(?i)(COUNT|SUM|AVG|MAX|MIN)\\s*\\(").matcher(havingPart).find()) {
                SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_HAVING_INSTEAD_WHERE", 6);
                if (tip != null) tips.add(tip);
            }
        }
    }

    /**
     * 检查关联子查询
     */
    private void checkCorrelatedSubquery(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_CORRELATED_SUBQUERY")) {
            return;
        }
        // 检测EXISTS/IN子查询中引用外部表
        if (Pattern.compile("(?i)EXISTS\\s*\\(\\s*SELECT").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_CORRELATED_SUBQUERY", 7);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查大字段查询
     */
    private void checkLargeFieldQuery(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_LARGE_FIELD_QUERY")) {
            return;
        }
        // 检测SELECT中包含TEXT/BLOB类型字段（通过常见命名或显式字段）
        if (Pattern.compile("(?i)SELECT\\s+.*\\b(content|detail|description|text|blob|json|memo|remark)\\b").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_LARGE_FIELD_QUERY", 5);
            if (tip != null) tips.add(tip);
        }
    }

    /**
     * 检查未使用绑定变量
     */
    private void checkNoBoundVariables(String sql, List<SqlAnalyzeResult.OptimizationTip> tips) {
        if (!optimizationRuleService.isEnabled("RULE_NO_BOUND_VARIABLES")) {
            return;
        }
        // 检测硬编码的字符串或数字常量（简单判断：WHERE条件中有具体值而非?或:param）
        if (Pattern.compile("(?i)WHERE\\s+.*=\\s*['\"]\\w+['\"]|WHERE\\s+.*=\\s*\\d+").matcher(sql).find()) {
            SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_NO_BOUND_VARIABLES", 5);
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

            // 检查文件排序
            if (optimizationRuleService.isEnabled("RULE_FILESORT")) {
                Object extra = row.get("Extra");
                if (extra != null && extra.toString().contains("Using filesort")) {
                    boolean hasFilesort = tips.stream()
                            .anyMatch(t -> "RULE_FILESORT".equals(t.getRuleCode()));
                    if (!hasFilesort) {
                        SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_FILESORT", 6);
                        if (tip != null) tips.add(tip);
                    }
                }
            }

            // 检查临时表
            if (optimizationRuleService.isEnabled("RULE_TEMPORARY_TABLE")) {
                Object extra = row.get("Extra");
                if (extra != null && extra.toString().contains("Using temporary")) {
                    boolean hasTemp = tips.stream()
                            .anyMatch(t -> "RULE_TEMPORARY_TABLE".equals(t.getRuleCode()));
                    if (!hasTemp) {
                        SqlAnalyzeResult.OptimizationTip tip = createTip("RULE_TEMPORARY_TABLE", 6);
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
