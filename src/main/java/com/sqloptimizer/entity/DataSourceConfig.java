package com.sqloptimizer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据源配置实体
 */
@Data
@TableName("data_source_config")
public class DataSourceConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 数据源名称 */
    private String name;

    /** 数据库类型(mysql/postgresql/oracle) */
    private String dbType;

    /** 主机地址 */
    private String host;

    /** 端口 */
    private Integer port;

    /** 数据库名 */
    private String databaseName;

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    /** 描述 */
    private String description;

    /** 状态(0-禁用,1-启用) */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除(0-未删除,1-已删除) */
    @TableLogic
    private Integer deleted;
}