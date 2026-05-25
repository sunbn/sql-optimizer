package com.sqloptimizer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqloptimizer.dto.ApiResponse;
import com.sqloptimizer.entity.SlowLog;
import com.sqloptimizer.service.SlowLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 慢查询日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/slowlog")
@RequiredArgsConstructor
public class SlowLogController {

    private final SlowLogService slowLogService;

    /**
     * 分页查询慢查询日志
     */
    @GetMapping("/page")
    public ApiResponse<Page<SlowLog>> pageQuery(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long dataSourceId,
            @RequestParam(required = false) Integer analyzed) {
        Page<SlowLog> page = new Page<>(current, size);
        Page<SlowLog> result = slowLogService.pageQuery(page, dataSourceId, analyzed);
        return ApiResponse.success(result);
    }

    /**
     * 获取未分析的慢查询
     */
    @GetMapping("/unanalyzed")
    public ApiResponse<List<SlowLog>> getUnanalyzedSlowLogs(
            @RequestParam(required = false) Long dataSourceId) {
        List<SlowLog> list = slowLogService.getUnanalyzedSlowLogs(dataSourceId);
        return ApiResponse.success(list);
    }

    /**
     * 标记为已分析
     */
    @PostMapping("/mark-analyzed/{id}")
    public ApiResponse<Void> markAsAnalyzed(
            @PathVariable Long id,
            @RequestParam Long analysisResultId) {
        slowLogService.markAsAnalyzed(id, analysisResultId);
        return ApiResponse.success();
    }

    /**
     * 统计慢查询数量
     */
    @GetMapping("/count")
    public ApiResponse<Long> countSlowLogs(
            @RequestParam(required = false) Long dataSourceId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        long count = slowLogService.countSlowLogs(dataSourceId, startTime, endTime);
        return ApiResponse.success(count);
    }
}