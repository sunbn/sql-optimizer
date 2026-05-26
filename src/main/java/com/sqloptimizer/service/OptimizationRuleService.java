package com.sqloptimizer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqloptimizer.entity.OptimizationRule;
import com.sqloptimizer.mapper.OptimizationRuleMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 优化规则服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OptimizationRuleService {

    private final OptimizationRuleMapper optimizationRuleMapper;

    /** 规则缓存 */
    private final Map<String, OptimizationRule> ruleCache = new ConcurrentHashMap<>();

    /**
     * 初始化加载规则到缓存
     */
    @PostConstruct
    public void init() {
        refreshCache();
    }

    /**
     * 刷新规则缓存
     */
    public void refreshCache() {
        LambdaQueryWrapper<OptimizationRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OptimizationRule::getEnabled, 1);
        List<OptimizationRule> rules = optimizationRuleMapper.selectList(wrapper);
        
        ruleCache.clear();
        for (OptimizationRule rule : rules) {
            ruleCache.put(rule.getRuleCode(), rule);
        }
        log.info("加载优化规则 {} 条", ruleCache.size());
    }

    /**
     * 获取启用的规则
     */
    public OptimizationRule getRule(String ruleCode) {
        return ruleCache.get(ruleCode);
    }

    /**
     * 判断规则是否启用
     */
    public boolean isEnabled(String ruleCode) {
        return ruleCache.containsKey(ruleCode);
    }

    /**
     * 获取规则优先级
     */
    public int getPriority(String ruleCode, int defaultPriority) {
        OptimizationRule rule = ruleCache.get(ruleCode);
        return rule != null ? rule.getPriority() : defaultPriority;
    }

    /**
     * 获取所有规则
     */
    public List<OptimizationRule> listAll() {
        return optimizationRuleMapper.selectList(null);
    }

    /**
     * 更新规则
     */
    public void updateRule(OptimizationRule rule) {
        optimizationRuleMapper.updateById(rule);
        refreshCache();
    }

    /**
     * 切换规则开关
     */
    public void toggleRule(Long id, Integer enabled) {
        OptimizationRule rule = new OptimizationRule();
        rule.setId(id);
        rule.setEnabled(enabled);
        optimizationRuleMapper.updateById(rule);
        refreshCache();
    }
}