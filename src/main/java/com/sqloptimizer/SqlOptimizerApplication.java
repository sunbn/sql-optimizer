package com.sqloptimizer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SQL优化工具启动类
 */
@SpringBootApplication
@MapperScan("com.sqloptimizer.mapper")
public class SqlOptimizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqlOptimizerApplication.class, args);
    }
}