package com.sqloptimizer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqloptimizer.entity.DataSourceConfig;
import com.sqloptimizer.entity.SlowLog;
import com.sqloptimizer.mapper.DataSourceConfigMapper;
import com.sqloptimizer.service.SlowLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 页面控制器
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final DataSourceConfigMapper dataSourceConfigMapper;
    private final SlowLogService slowLogService;

    /**
     * 首页 - SQL分析
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "SQL分析");
        model.addAttribute("activeMenu", "analyze");
        return "index";
    }

    /**
     * 慢查询日志页面
     */
    @GetMapping("/slowlog/page")
    public String slowLogPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            Model model) {
        Page<SlowLog> page = new Page<>(current, size);
        Page<SlowLog> result = slowLogService.pageQuery(page, null, null);
        model.addAttribute("page", result);
        model.addAttribute("pageTitle", "慢查询日志");
        model.addAttribute("activeMenu", "slowlog");
        return "slowlog";
    }

    /**
     * 数据源管理页面
     */
    @GetMapping("/datasource/page")
    public String dataSourcePage(Model model) {
        model.addAttribute("dataSources", dataSourceConfigMapper.selectList(null));
        model.addAttribute("pageTitle", "数据源管理");
        model.addAttribute("activeMenu", "datasource");
        return "datasource";
    }
}