package com.sqloptimizer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SQL分析记录实体
 */
@Data
@TableName("sql_analysis_record")
public class SqlAnalysisRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 数据源ID */
    private Long dataSourceId;

    /** SQL内容 */
    private String sqlContent;

    /** SQL哈希值 */
    private String sqlHash;

    /** SQL类型(SELECT/INSERT/UPDATE/DELETE) */
    private String sqlType;

    /** 涉及的表 */
    private String tables;

    /** 执行时间(ms) */
    private Long executionTime;

    /** 扫描行数 */
    private Long rowsExamined;

    /** 返回行数 */
    private Long rowsSent;

    /** 执行计划结果(JSON) */
    private String explainResult;

    /** 索引建议(JSON) */
    private String indexSuggestions;

    /** 优化建议(JSON) */
    private String optimizationTips;

    /** SQL评分(0-100) */
    private Integer score;

    /** 分析状态 */
    private String status;

    /** 错误信息 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}