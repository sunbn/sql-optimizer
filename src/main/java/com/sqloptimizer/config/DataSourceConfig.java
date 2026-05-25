package com.sqloptimizer.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置类
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    /**
     * 主数据源配置
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    /**
     * 主数据源
     */
    @Bean
    @Primary
    public DataSource dataSource(HikariConfig hikariConfig) {
        log.info("初始化主数据源...");
        return new HikariDataSource(hikariConfig);
    }

    /**
     * MyBatis Plus 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}