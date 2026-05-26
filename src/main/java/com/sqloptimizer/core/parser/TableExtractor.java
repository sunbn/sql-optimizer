package com.sqloptimizer.core.parser;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 表名提取器
 */
@Slf4j
@Component
public class TableExtractor {

    /**
     * 从SQL中提取所有表名
     */
    public List<String> extractTables(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableSet = tablesNamesFinder.getTableList(statement);
            return new ArrayList<>(tableSet);
        } catch (Exception e) {
            log.error("提取表名失败: {}", sql, e);
            return new ArrayList<>();
        }
    }

    /**
     * 从Statement中提取表名
     */
    public List<String> extractTables(Statement statement) {
        try {
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableSet = tablesNamesFinder.getTableList(statement);
            return new ArrayList<>(tableSet);
        } catch (Exception e) {
            log.error("提取表名失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取主表名
     */
    public String getMainTable(Statement statement) {
        try {
            if (statement instanceof Select) {
                Select select = (Select) statement;
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                FromItem fromItem = plainSelect.getFromItem();
                if (fromItem instanceof Table) {
                    return ((Table) fromItem).getName();
                }
            } else if (statement instanceof Update) {
                return ((Update) statement).getTable().getName();
            } else if (statement instanceof Insert) {
                return ((Insert) statement).getTable().getName();
            } else if (statement instanceof Delete) {
                return ((Delete) statement).getTable().getName();
            }
        } catch (Exception e) {
            log.error("获取主表失败", e);
        }
        return null;
    }
}