package com.sqloptimizer.core.parser;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.springframework.stereotype.Component;

/**
 * SQL解析器
 */
@Slf4j
@Component
public class SqlParser {

    /**
     * 解析SQL语句
     */
    public Statement parse(String sql) {
        try {
            return CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            log.error("SQL解析失败: {}", sql, e);
            throw new RuntimeException("SQL解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取SQL类型
     */
    public String getSqlType(String sql) {
        try {
            Statement statement = parse(sql);
            if (statement instanceof Select) {
                return "SELECT";
            } else if (statement instanceof Insert) {
                return "INSERT";
            } else if (statement instanceof Update) {
                return "UPDATE";
            } else if (statement instanceof Delete) {
                return "DELETE";
            }
            return "UNKNOWN";
        } catch (Exception e) {
            log.error("获取SQL类型失败", e);
            return "UNKNOWN";
        }
    }

    /**
     * 验证SQL语法
     */
    public boolean validate(String sql) {
        try {
            parse(sql);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}