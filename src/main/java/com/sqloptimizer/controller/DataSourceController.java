package com.sqloptimizer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqloptimizer.dto.ApiResponse;
import com.sqloptimizer.entity.DataSourceConfig;
import com.sqloptimizer.mapper.DataSourceConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据源配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/datasource")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceConfigMapper dataSourceConfigMapper;

    /**
     * 获取所有数据源
     */
    @GetMapping("/list")
    public ApiResponse<List<DataSourceConfig>> list() {
        LambdaQueryWrapper<DataSourceConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataSourceConfig::getStatus, 1);
        List<DataSourceConfig> list = dataSourceConfigMapper.selectList(wrapper);
        return ApiResponse.success(list);
    }

    /**
     * 根据ID获取数据源
     */
    @GetMapping("/{id}")
    public ApiResponse<DataSourceConfig> getById(@PathVariable Long id) {
        DataSourceConfig config = dataSourceConfigMapper.selectById(id);
        return ApiResponse.success(config);
    }

    /**
     * 保存数据源
     */
    @PostMapping("/save")
    public ApiResponse<Void> save(@RequestBody DataSourceConfig config) {
        dataSourceConfigMapper.insert(config);
        return ApiResponse.success();
    }

    /**
     * 更新数据源
     */
    @PostMapping("/update")
    public ApiResponse<Void> update(@RequestBody DataSourceConfig config) {
        dataSourceConfigMapper.updateById(config);
        return ApiResponse.success();
    }

    /**
     * 删除数据源
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dataSourceConfigMapper.deleteById(id);
        return ApiResponse.success();
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/test-connection")
    public ApiResponse<Boolean> testConnection(@RequestBody DataSourceConfig config) {
        // TODO: 实现连接测试逻辑
        return ApiResponse.success(true);
    }
}