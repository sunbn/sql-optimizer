package com.sqloptimizer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优化规则实体
 */
@Data
@TableName("optimization_rule")
public class OptimizationRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编码 */
    private String ruleCode;

    /** 规则名称 */
    private String ruleName;

    /** 规则类型(INDEX/PERFORMANCE/SECURITY) */
    private String ruleType;

    /** 规则描述 */
    private String description;

    /** 优化建议 */
    private String suggestion;

    /** 优先级(1-10) */
    private Integer priority;

    /** 是否启用(0-禁用,1-启用) */
    private Integer enabled;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}