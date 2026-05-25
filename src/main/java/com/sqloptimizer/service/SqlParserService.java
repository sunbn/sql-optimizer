package com.sqloptimizer.service;

import com.sqloptimizer.core.parser.SqlParser;
import com.sqloptimizer.core.parser.TableExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SQL解析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlParserService {

    private final SqlParser sqlParser;
    private final TableExtractor tableExtractor;

    /**
     * 验证SQL语法
     */
    public boolean validateSql(String sql) {
        return sqlParser.validate(sql);
    }

    /**
     * 获取SQL类型
     */
    public String getSqlType(String sql) {
        return sqlParser.getSqlType(sql);
    }

    /**
     * 提取SQL涉及的表
     */
    public List<String> extractTables(String sql) {
        return tableExtractor.extractTables(sql);
    }
}