-- SQL优化工具数据库初始化脚本

CREATE DATABASE IF NOT EXISTS sql_optimizer DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sql_optimizer;

-- 数据源配置表
CREATE TABLE IF NOT EXISTS data_source_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    db_type VARCHAR(20) NOT NULL COMMENT '数据库类型(mysql/postgresql/oracle)',
    host VARCHAR(100) NOT NULL COMMENT '主机地址',
    port INT NOT NULL COMMENT '端口',
    database_name VARCHAR(100) NOT NULL COMMENT '数据库名',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除(0-未删除,1-已删除)',
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- SQL分析记录表
CREATE TABLE IF NOT EXISTS sql_analysis_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_source_id BIGINT COMMENT '数据源ID',
    sql_content TEXT NOT NULL COMMENT 'SQL内容',
    sql_hash VARCHAR(64) NOT NULL COMMENT 'SQL哈希值',
    sql_type VARCHAR(20) COMMENT 'SQL类型(SELECT/INSERT/UPDATE/DELETE)',
    tables TEXT COMMENT '涉及的表',
    execution_time BIGINT COMMENT '执行时间(ms)',
    rows_examined BIGINT COMMENT '扫描行数',
    rows_sent BIGINT COMMENT '返回行数',
    explain_result JSON COMMENT '执行计划结果',
    index_suggestions JSON COMMENT '索引建议',
    optimization_tips JSON COMMENT '优化建议',
    score INT COMMENT 'SQL评分(0-100)',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '分析状态',
    error_msg TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_sql_hash (sql_hash),
    INDEX idx_data_source_id (data_source_id),
    INDEX idx_created_at (created_at),
    INDEX idx_sql_type (sql_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SQL分析记录表';

-- 慢查询日志表
CREATE TABLE IF NOT EXISTS slow_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    data_source_id BIGINT NOT NULL COMMENT '数据源ID',
    sql_content TEXT NOT NULL COMMENT 'SQL内容',
    sql_hash VARCHAR(64) NOT NULL COMMENT 'SQL哈希值',
    execution_time BIGINT NOT NULL COMMENT '执行时间(ms)',
    lock_time BIGINT COMMENT '锁等待时间(ms)',
    rows_examined BIGINT COMMENT '扫描行数',
    rows_sent BIGINT COMMENT '返回行数',
    query_time DATETIME COMMENT '查询发生时间',
    client_host VARCHAR(100) COMMENT '客户端地址',
    user_name VARCHAR(100) COMMENT '执行用户',
    analyzed TINYINT DEFAULT 0 COMMENT '是否已分析(0-未分析,1-已分析)',
    analysis_result_id BIGINT COMMENT '关联分析记录ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_sql_hash (sql_hash),
    INDEX idx_data_source_id (data_source_id),
    INDEX idx_query_time (query_time),
    INDEX idx_analyzed (analyzed),
    INDEX idx_execution_time (execution_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='慢查询日志表';

-- 索引建议记录表
CREATE TABLE IF NOT EXISTS index_advice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    analysis_record_id BIGINT NOT NULL COMMENT '分析记录ID',
    table_name VARCHAR(100) NOT NULL COMMENT '表名',
    index_columns VARCHAR(500) NOT NULL COMMENT '建议索引列',
    index_type VARCHAR(20) DEFAULT 'BTREE' COMMENT '索引类型',
    reason TEXT COMMENT '建议原因',
    estimated_improvement VARCHAR(100) COMMENT '预估提升',
    applied TINYINT DEFAULT 0 COMMENT '是否已应用(0-未应用,1-已应用)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_analysis_record_id (analysis_record_id),
    INDEX idx_table_name (table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='索引建议记录表';

-- 优化规则表
CREATE TABLE IF NOT EXISTS optimization_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_code VARCHAR(50) NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(20) NOT NULL COMMENT '规则类型(INDEX/PERFORMANCE/SECURITY)',
    description TEXT COMMENT '规则描述',
    suggestion TEXT COMMENT '优化建议',
    priority INT DEFAULT 5 COMMENT '优先级(1-10)',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用(0-禁用,1-启用)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_rule_code (rule_code),
    INDEX idx_rule_type (rule_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优化规则表';

-- 初始化优化规则数据
INSERT INTO optimization_rule (rule_code, rule_name, rule_type, description, suggestion, priority) VALUES
('RULE_SELECT_STAR', '避免SELECT *', 'PERFORMANCE', '使用SELECT *会返回所有列，增加网络传输和内存消耗', '明确指定需要的列，避免返回不必要的数据', 8),
('RULE_MISSING_INDEX', '缺少索引', 'INDEX', 'WHERE条件中的列缺少索引，导致全表扫描', '为WHERE条件中的列添加索引', 10),
('RULE_FULL_TABLE_SCAN', '全表扫描', 'PERFORMANCE', 'SQL执行计划中出现全表扫描', '优化查询条件或添加合适的索引', 9),
('RULE_IMPLICIT_CONVERSION', '隐式类型转换', 'PERFORMANCE', 'WHERE条件中存在隐式类型转换，导致索引失效', '确保比较双方类型一致', 8),
('RULE_LIKE_PREFIX', 'LIKE前缀模糊查询', 'PERFORMANCE', 'LIKE以%开头会导致索引失效', '考虑使用全文索引或优化查询方式', 7),
('RULE_OR_CONDITION', 'OR条件优化', 'PERFORMANCE', 'OR条件可能导致索引失效', '考虑使用UNION ALL替代OR', 6),
('RULE_SUBQUERY', '子查询优化', 'PERFORMANCE', '子查询可能导致性能问题', '考虑使用JOIN替代子查询', 6),
('RULE_ORDER_BY', 'ORDER BY优化', 'PERFORMANCE', 'ORDER BY使用不当可能导致文件排序', '确保ORDER BY列有索引', 5),
('RULE_LIMIT_OFFSET', 'LIMIT大偏移量', 'PERFORMANCE', 'LIMIT大偏移量会导致扫描大量数据', '使用覆盖索引或优化分页方式', 7),
('RULE_IN_CLAUSE', 'IN子句数量过多', 'PERFORMANCE', 'IN子句中元素过多影响性能', '考虑使用临时表或分批处理', 5);