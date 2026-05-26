package com.sqloptimizer.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis XML SQL 格式化工具
 * 将 MyBatis Mapper XML 中的 SQL 片段清洗为标准 SQL
 */
@Slf4j
public class SqlFormatter {

    /**
     * 格式化 MyBatis XML SQL 为标准 SQL
     */
    public static String format(String rawSql) {
        if (rawSql == null || rawSql.trim().isEmpty()) {
            return rawSql;
        }

        String sql = rawSql;

        // 1. 移除 XML 注释 <!-- ... -->
        sql = removeXmlComments(sql);

        // 2. 处理 <where> 标签
        sql = replaceTag(sql, "where", "WHERE");

        // 3. 处理 <set> 标签
        sql = replaceTag(sql, "set", "SET");

        // 4. 处理 <trim> 标签（保留内容，移除标签本身）
        sql = removeMybatisTag(sql, "trim");

        // 5. 处理 <if> 标签（保留条件内容）
        sql = removeMybatisTag(sql, "if");

        // 6. 处理 <choose>/<when>/<otherwise> 标签
        sql = removeMybatisTag(sql, "choose");
        sql = removeMybatisTag(sql, "when");
        sql = removeMybatisTag(sql, "otherwise");

        // 7. 处理 <foreach> 标签（简化为 IN (...)）
        sql = simplifyForeach(sql);

        // 8. 处理 <bind> 标签
        sql = removeMybatisTag(sql, "bind");

        // 9. 替换 MyBatis 参数占位符 #{} 和 ${} 为 ?
        sql = replaceParameters(sql);

        // 10. 清理多余空白和换行
        sql = cleanWhitespace(sql);

        log.debug("SQL格式化前: {}", rawSql);
        log.debug("SQL格式化后: {}", sql);

        return sql;
    }

    /**
     * 移除 XML 注释 <!-- ... -->
     */
    private static String removeXmlComments(String sql) {
        return sql.replaceAll("<!--[\\s\\S]*?-->", " ");
    }

    /**
     * 替换 MyBatis 标签为 SQL 关键字（如 <where> -> WHERE）
     */
    private static String replaceTag(String sql, String tagName, String replacement) {
        // 移除开始标签 <tag ...>
        sql = sql.replaceAll("(?i)<" + tagName + "\\b[^>]*>", " " + replacement + " ");
        // 移除结束标签 </tag>
        sql = sql.replaceAll("(?i)</" + tagName + "\\s*>", " ");
        return sql;
    }

    /**
     * 移除 MyBatis 标签，保留标签内部内容
     */
    private static String removeMybatisTag(String sql, String tagName) {
        // 移除开始标签 <tag ...>
        sql = sql.replaceAll("(?i)<" + tagName + "\\b[^>]*>", " ");
        // 移除结束标签 </tag>
        sql = sql.replaceAll("(?i)</" + tagName + "\\s*>", " ");
        return sql;
    }

    /**
     * 简化 <foreach> 标签为 IN (?, ?, ?)
     */
    private static String simplifyForeach(String sql) {
        // 匹配 <foreach ...> ... </foreach>
        Pattern pattern = Pattern.compile("(?i)<foreach\\b[^>]*>([\\s\\S]*?)</foreach\\s*>");
        Matcher matcher = pattern.matcher(sql);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String content = matcher.group(1).trim();
            // 提取 item 和 separator
            String fullTag = matcher.group(0);
            String item = extractAttribute(fullTag, "item");
            String separator = extractAttribute(fullTag, "separator");
            String open = extractAttribute(fullTag, "open");
            String close = extractAttribute(fullTag, "close");

            // 构建简化版本
            StringBuilder replacement = new StringBuilder();
            if (open != null && !open.isEmpty()) {
                replacement.append(open);
            }
            // 将内容中的 item 引用替换为 ?
            String simplified = content.replaceAll("(?i)\\b" + Pattern.quote(item) + "\\b", "?");
            replacement.append(simplified);
            if (close != null && !close.isEmpty()) {
                replacement.append(close);
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 从标签中提取属性值
     */
    private static String extractAttribute(String tag, String attrName) {
        Pattern pattern = Pattern.compile("(?i)" + attrName + "\\s*=\\s*[\"']([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(tag);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 替换 #{} 和 ${} 参数为 ?
     */
    private static String replaceParameters(String sql) {
        // 替换 #{...} 为 ?
        sql = sql.replaceAll("#\\{[^}]+\\}", "?");
        // 替换 ${...} 为 ?
        sql = sql.replaceAll("\\$\\{[^}]+\\}", "?");
        return sql;
    }

    /**
     * 清理多余空白和换行
     */
    private static String cleanWhitespace(String sql) {
        // 将多个空白字符替换为单个空格
        sql = sql.replaceAll("\\s+", " ");
        // 清理逗号前后的空格
        sql = sql.replaceAll("\\s*,\\s*", ", ");
        // 清理关键字前后的空格
        sql = sql.replaceAll("\\s*\\(\\s*", " (");
        sql = sql.replaceAll("\\s*\\)\\s*", ") ");
        return sql.trim();
    }

    /**
     * 判断是否为 MyBatis XML SQL（包含 MyBatis 标签特征）
     */
    public static boolean isMybatisXmlSql(String sql) {
        if (sql == null) {
            return false;
        }
        return sql.contains("<if") || sql.contains("<where") || sql.contains("<foreach")
                || sql.contains("<choose") || sql.contains("<trim") || sql.contains("<set")
                || sql.contains("<bind") || sql.contains("<!--") || sql.contains("#{")
                || sql.contains("${");
    }
}