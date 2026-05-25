package com.sqloptimizer.core.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EXPLAIN执行计划分析器
 */
@Slf4j
@Component
public class ExplainAnalyzer {

    /**
     * 执行EXPLAIN分析
     */
    public List<Map<String, Object>> analyze(DataSource dataSource, String sql) {
        List<Map<String, Object>> result = new ArrayList<>();
        String explainSql = "EXPLAIN " + sql;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(explainSql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                result.add(row);
            }

        } catch (SQLException e) {
            log.error("执行EXPLAIN分析失败: {}", sql, e);
            throw new RuntimeException("EXPLAIN分析失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 执行EXPLAIN FORMAT=JSON分析(MySQL)
     */
    public String analyzeJson(DataSource dataSource, String sql) {
        String explainSql = "EXPLAIN FORMAT=JSON " + sql;
        StringBuilder result = new StringBuilder();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(explainSql)) {

            while (rs.next()) {
                result.append(rs.getString(1));
            }

        } catch (SQLException e) {
            log.error("执行EXPLAIN JSON分析失败: {}", sql, e);
            throw new RuntimeException("EXPLAIN JSON分析失败: " + e.getMessage());
        }

        return result.toString();
    }

    /**
     * 检查是否使用索引
     */
    public boolean isUsingIndex(Map<String, Object> explainRow) {
        Object key = explainRow.get("key");
        return key != null && !key.toString().isEmpty() && !"NULL".equals(key.toString());
    }

    /**
     * 检查是否全表扫描
     */
    public boolean isFullTableScan(Map<String, Object> explainRow) {
        Object type = explainRow.get("type");
        return "ALL".equals(type);
    }

    /**
     * 获取扫描行数
     */
    public Long getRowsExamined(Map<String, Object> explainRow) {
        Object rows = explainRow.get("rows");
        if (rows != null) {
            try {
                return Long.parseLong(rows.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}