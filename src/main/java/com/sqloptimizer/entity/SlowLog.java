package com.sqloptimizer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 慢查询日志实体
 */
@Data
@TableName("slow_log")
public class SlowLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 数据源ID */
    private Long dataSourceId;

    /** SQL内容 */
    private String sqlContent;

    /** SQL哈希值 */
    private String sqlHash;

    /** 执行时间(ms) */
    private Long executionTime;

    /** 锁等待时间(ms) */
    private Long lockTime;

    /** 扫描行数 */
    private Long rowsExamined;

    /** 返回行数 */
    private Long rowsSent;

    /** 查询发生时间 */
    private LocalDateTime queryTime;

    /** 客户端地址 */
    private String clientHost;

    /** 执行用户 */
    private String userName;

    /** 是否已分析(0-未分析,1-已分析) */
    private Integer analyzed;

    /** 关联分析记录ID */
    private Long analysisResultId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}