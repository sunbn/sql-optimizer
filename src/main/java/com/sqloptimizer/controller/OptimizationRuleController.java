package com.sqloptimizer.controller;

import com.sqloptimizer.dto.ApiResponse;
import com.sqloptimizer.entity.OptimizationRule;
import com.sqloptimizer.service.OptimizationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优化规则管理控制器
 */
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class OptimizationRuleController {

    private final OptimizationRuleService optimizationRuleService;

    /**
     * 获取所有规则
     */
    @GetMapping
    public ApiResponse<List<OptimizationRule>> listRules() {
        return ApiResponse.success(optimizationRuleService.listAll());
    }

    /**
     * 更新规则
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateRule(@PathVariable Long id, @RequestBody OptimizationRule rule) {
        rule.setId(id);
        optimizationRuleService.updateRule(rule);
        return ApiResponse.success();
    }

    /**
     * 切换规则开关
     */
    @PostMapping("/{id}/toggle")
    public ApiResponse<Void> toggleRule(@PathVariable Long id, @RequestParam Integer enabled) {
        optimizationRuleService.toggleRule(id, enabled);
        return ApiResponse.success();
    }

    /**
     * 刷新规则缓存
     */
    @PostMapping("/refresh")
    public ApiResponse<Void> refreshCache() {
        optimizationRuleService.refreshCache();
        return ApiResponse.success();
    }
}