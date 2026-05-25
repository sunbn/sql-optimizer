package com.sqloptimizer.util;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * SQL哈希工具类
 */
public class SqlHashUtil {

    /**
     * 计算SQL的MD5哈希值
     */
    public static String hash(String sql) {
        if (sql == null) {
            return null;
        }
        // 去除多余空白并转为小写
        String normalized = sql.replaceAll("\\s+", " ").trim().toLowerCase();
        return DigestUtil.md5Hex(normalized);
    }
}