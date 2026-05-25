package com.sqloptimizer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 索引建议实体
 */
@Data
@TableName("index_advice")
public class IndexAdvice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分析记录ID */
    private Long analysisRecordId;

    /** 表名 */
    private String tableName;

    /** 建议索引列 */
    private String indexColumns;

    /** 索引类型 */
    private String indexType;

    /** 建议原因 */
    private String reason;

    /** 预估提升 */
    private String estimatedImprovement;

    /** 是否已应用(0-未应用,1-已应用) */
    private Integer applied;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}