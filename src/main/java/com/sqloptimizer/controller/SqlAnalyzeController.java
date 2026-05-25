package com.sqloptimizer.controller;

import com.sqloptimizer.dto.ApiResponse;
import com.sqloptimizer.dto.SqlAnalyzeRequest;
import com.sqloptimizer.dto.SqlAnalyzeResult;
import com.sqloptimizer.service.ExplainService;
import com.sqloptimizer.service.IndexAdviceService;
import com.sqloptimizer.service.SqlParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SQL分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/sql")
@RequiredArgsConstructor
public class SqlAnalyzeController {

    private final SqlParserService sqlParserService;
    private final ExplainService explainService;
    private final IndexAdviceService indexAdviceService;

    /**
     * 分析SQL
     */
    @PostMapping("/analyze")
    public ApiResponse<SqlAnalyzeResult> analyzeSql(@Valid @RequestBody SqlAnalyzeRequest request) {
        log.info("收到SQL分析请求");

        SqlAnalyzeResult result = new SqlAnalyzeResult();
        String sql = request.getSqlContent();

        try {
            // 1. 验证SQL语法
            if (!sqlParserService.validateSql(sql)) {
                result.setStatus("FAILED");
                result.setErrorMsg("SQL语法验证失败");
                return ApiResponse.success(result);
            }

            // 2. 获取SQL类型
            String sqlType = sqlParserService.getSqlType(sql);
            result.setSqlType(sqlType);

            // 3. 提取涉及的表
            List<String> tables = sqlParserService.extractTables(sql);
            result.setTables(tables);

            // 4. 执行EXPLAIN分析（如果有数据源）
            if (request.getDataSourceId() != null && request.getExecuteExplain()) {
                // TODO: 获取数据源连接并执行EXPLAIN
                // 这里简化处理，实际应该根据dataSourceId获取对应的数据源
                result.setExplainResult(List.of());
            }

            // 5. 生成索引建议
            if (request.getGenerateIndexAdvice()) {
                List<SqlAnalyzeResult.IndexSuggestion> suggestions =
                        indexAdviceService.generateIndexAdvice(sql, result.getExplainResult());
                result.setIndexSuggestions(suggestions);
            }

            // 6. 执行优化规则检查
            if (request.getCheckOptimizationRules()) {
                List<SqlAnalyzeResult.OptimizationTip> tips =
                        indexAdviceService.checkOptimizationRules(sql, result.getExplainResult());
                result.setOptimizationTips(tips);
            }

            // 7. 计算得分
            int score = explainService.evaluateScore(result.getExplainResult());
            result.setScore(score);
            result.setStatus("SUCCESS");

        } catch (Exception e) {
            log.error("SQL分析失败", e);
            result.setStatus("FAILED");
            result.setErrorMsg(e.getMessage());
        }

        return ApiResponse.success(result);
    }

    /**
     * 验证SQL语法
     */
    @PostMapping("/validate")
    public ApiResponse<Boolean> validateSql(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        boolean valid = sqlParserService.validateSql(sql);
        return ApiResponse.success(valid);
    }

    /**
     * 获取SQL类型
     */
    @PostMapping("/type")
    public ApiResponse<String> getSqlType(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        String type = sqlParserService.getSqlType(sql);
        return ApiResponse.success(type);
    }

    /**
     * 提取SQL涉及的表
     */
    @PostMapping("/tables")
    public ApiResponse<List<String>> extractTables(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        List<String> tables = sqlParserService.extractTables(sql);
        return ApiResponse.success(tables);
    }
}