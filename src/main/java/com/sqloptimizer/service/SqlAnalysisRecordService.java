package com.sqloptimizer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqloptimizer.dto.SqlAnalyzeResult;
import com.sqloptimizer.entity.IndexAdvice;
import com.sqloptimizer.entity.SqlAnalysisRecord;
import com.sqloptimizer.mapper.IndexAdviceMapper;
import com.sqloptimizer.mapper.SqlAnalysisRecordMapper;
import com.sqloptimizer.util.SqlHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SQL分析记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlAnalysisRecordService {

    private final SqlAnalysisRecordMapper sqlAnalysisRecordMapper;
    private final IndexAdviceMapper indexAdviceMapper;
    private final ObjectMapper objectMapper;

    /**
     * 保存SQL分析结果
     */
    @Transactional
    public SqlAnalysisRecord saveAnalysisResult(Long dataSourceId, String sql,
                                                 SqlAnalyzeResult result) {
        SqlAnalysisRecord record = new SqlAnalysisRecord();
        record.setDataSourceId(dataSourceId);
        record.setSqlContent(sql);
        record.setSqlHash(SqlHashUtil.hash(sql));
        record.setSqlType(result.getSqlType());
        record.setTables(joinTables(result.getTables()));
        record.setScore(result.getScore());
        record.setStatus(result.getStatus());
        record.setErrorMsg(result.getErrorMsg());

        // 序列化执行计划
        if (result.getExplainResult() != null) {
            try {
                record.setExplainResult(objectMapper.writeValueAsString(result.getExplainResult()));
            } catch (JsonProcessingException e) {
                log.error("序列化执行计划失败", e);
            }
        }

        // 序列化索引建议
        if (result.getIndexSuggestions() != null) {
            try {
                record.setIndexSuggestions(objectMapper.writeValueAsString(result.getIndexSuggestions()));
            } catch (JsonProcessingException e) {
                log.error("序列化索引建议失败", e);
            }
        }

        // 序列化优化提示
        if (result.getOptimizationTips() != null) {
            try {
                record.setOptimizationTips(objectMapper.writeValueAsString(result.getOptimizationTips()));
            } catch (JsonProcessingException e) {
                log.error("序列化优化提示失败", e);
            }
        }

        sqlAnalysisRecordMapper.insert(record);
        log.info("保存SQL分析记录, id={}", record.getId());

        // 保存索引建议到独立表
        if (result.getIndexSuggestions() != null) {
            for (SqlAnalyzeResult.IndexSuggestion suggestion : result.getIndexSuggestions()) {
                IndexAdvice advice = new IndexAdvice();
                advice.setAnalysisRecordId(record.getId());
                advice.setTableName(suggestion.getTableName());
                advice.setIndexColumns(suggestion.getIndexColumns());
                advice.setIndexType(suggestion.getIndexType());
                advice.setReason(suggestion.getReason());
                advice.setEstimatedImprovement(suggestion.getEstimatedImprovement());
                indexAdviceMapper.insert(advice);
            }
            log.info("保存索引建议 {} 条", result.getIndexSuggestions().size());
        }

        return record;
    }

    private String joinTables(List<String> tables) {
        if (tables == null || tables.isEmpty()) {
            return null;
        }
        return String.join(",", tables);
    }
}