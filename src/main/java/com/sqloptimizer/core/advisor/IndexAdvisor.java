package com.sqloptimizer.core.advisor;

import com.sqloptimizer.dto.SqlAnalyzeResult;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 索引建议器
 */
@Slf4j
@Component
public class IndexAdvisor {

    // WHERE条件列提取正则
    private static final Pattern WHERE_COLUMN_PATTERN = Pattern.compile(
            "(?i)WHERE\\s+.*?([a-zA-Z_][a-zA-Z0-9_]*)\\s*[=<>!]",
            Pattern.DOTALL
    );

    // ORDER BY列提取正则
    private static final Pattern ORDER_BY_PATTERN = Pattern.compile(
            "(?i)ORDER\\s+BY\\s+([a-zA-Z_][a-zA-Z0-9_]*(?:\\s*,\\s*[a-zA-Z_][a-zA-Z0-9_]*)*)"
    );

    // JOIN条件列提取正则
    private static final Pattern JOIN_COLUMN_PATTERN = Pattern.compile(
            "(?i)JOIN\\s+\\w+\\s+ON\\s+.*?([a-zA-Z_][a-zA-Z0-9_]*)\\s*="
    );

    /**
     * 生成索引建议
     */
    public List<SqlAnalyzeResult.IndexSuggestion> generateIndexAdvice(String sql, List<Map<String, Object>> explainResult) {
        List<SqlAnalyzeResult.IndexSuggestion> suggestions = new ArrayList<>();

        try {
            // 解析SQL获取WHERE条件中的列
            List<String> whereColumns = extractWhereColumns(sql);

            // 解析SQL获取ORDER BY列
            List<String> orderByColumns = extractOrderByColumns(sql);

            // 解析SQL获取JOIN列
            List<String> joinColumns = extractJoinColumns(sql);

            // 获取涉及的表
            Statement statement = CCJSqlParserUtil.parse(sql);
            Set<String> tables = new TablesNamesFinder().getTableList(statement);

            for (String table : tables) {
                // 为WHERE条件列建议索引
                if (!whereColumns.isEmpty()) {
                    SqlAnalyzeResult.IndexSuggestion suggestion = new SqlAnalyzeResult.IndexSuggestion();
                    suggestion.setTableName(table);
                    suggestion.setIndexColumns(String.join(", ", whereColumns));
                    suggestion.setIndexType("BTREE");
                    suggestion.setReason("WHERE条件中的列缺少索引，添加索引可以加速数据过滤");
                    suggestion.setEstimatedImprovement("预计减少" + estimateImprovement(explainResult) + "%的扫描行数");
                    suggestions.add(suggestion);
                }

                // 为ORDER BY列建议索引
                if (!orderByColumns.isEmpty()) {
                    SqlAnalyzeResult.IndexSuggestion suggestion = new SqlAnalyzeResult.IndexSuggestion();
                    suggestion.setTableName(table);
                    suggestion.setIndexColumns(String.join(", ", orderByColumns));
                    suggestion.setIndexType("BTREE");
                    suggestion.setReason("ORDER BY使用文件排序，添加索引可以避免排序操作");
                    suggestion.setEstimatedImprovement("预计消除文件排序开销");
                    suggestions.add(suggestion);
                }

                // 为JOIN列建议索引
                if (!joinColumns.isEmpty()) {
                    SqlAnalyzeResult.IndexSuggestion suggestion = new SqlAnalyzeResult.IndexSuggestion();
                    suggestion.setTableName(table);
                    suggestion.setIndexColumns(String.join(", ", joinColumns));
                    suggestion.setIndexType("BTREE");
                    suggestion.setReason("JOIN条件中的列缺少索引，添加索引可以加速表连接");
                    suggestion.setEstimatedImprovement("预计减少JOIN操作时间");
                    suggestions.add(suggestion);
                }
            }

        } catch (Exception e) {
            log.error("生成索引建议失败", e);
        }

        return suggestions;
    }

    /**
     * 提取WHERE条件中的列
     */
    private List<String> extractWhereColumns(String sql) {
        List<String> columns = new ArrayList<>();
        Matcher matcher = WHERE_COLUMN_PATTERN.matcher(sql);
        while (matcher.find()) {
            String column = matcher.group(1);
            if (!columns.contains(column) && !isKeyword(column)) {
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * 提取ORDER BY列
     */
    private List<String> extractOrderByColumns(String sql) {
        List<String> columns = new ArrayList<>();
        Matcher matcher = ORDER_BY_PATTERN.matcher(sql);
        if (matcher.find()) {
            String orderByPart = matcher.group(1);
            String[] parts = orderByPart.split(",");
            for (String part : parts) {
                String column = part.trim().split("\\s+")[0];
                if (!columns.contains(column) && !isKeyword(column)) {
                    columns.add(column);
                }
            }
        }
        return columns;
    }

    /**
     * 提取JOIN列
     */
    private List<String> extractJoinColumns(String sql) {
        List<String> columns = new ArrayList<>();
        Matcher matcher = JOIN_COLUMN_PATTERN.matcher(sql);
        while (matcher.find()) {
            String column = matcher.group(1);
            if (!columns.contains(column) && !isKeyword(column)) {
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * 检查是否为SQL关键字
     */
    private boolean isKeyword(String word) {
        String[] keywords = {"AND", "OR", "NOT", "NULL", "IS", "IN", "BETWEEN", "LIKE"};
        for (String keyword : keywords) {
            if (keyword.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 预估性能提升百分比
     */
    private String estimateImprovement(List<Map<String, Object>> explainResult) {
        if (explainResult == null || explainResult.isEmpty()) {
            return "50";
        }

        long totalRows = 0;
        for (Map<String, Object> row : explainResult) {
            Object rows = row.get("rows");
            if (rows != null) {
                try {
                    totalRows += Long.parseLong(rows.toString());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (totalRows > 100000) {
            return "90";
        } else if (totalRows > 10000) {
            return "70";
        } else if (totalRows > 1000) {
            return "50";
        }
        return "30";
    }
}