package com.sqloptimizer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqloptimizer.entity.SlowLog;
import com.sqloptimizer.mapper.SlowLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 慢查询日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlowLogService {

    private final SlowLogMapper slowLogMapper;

    /**
     * 分页查询慢查询日志
     */
    public Page<SlowLog> pageQuery(Page<SlowLog> page, Long dataSourceId, Integer analyzed) {
        LambdaQueryWrapper<SlowLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dataSourceId != null, SlowLog::getDataSourceId, dataSourceId);
        wrapper.eq(analyzed != null, SlowLog::getAnalyzed, analyzed);
        wrapper.orderByDesc(SlowLog::getQueryTime);
        return slowLogMapper.selectPage(page, wrapper);
    }

    /**
     * 保存慢查询日志
     */
    public void saveSlowLog(SlowLog slowLog) {
        slowLogMapper.insert(slowLog);
    }

    /**
     * 批量保存慢查询日志
     */
    public void batchSaveSlowLogs(List<SlowLog> slowLogs) {
        for (SlowLog slowLog : slowLogs) {
            slowLogMapper.insert(slowLog);
        }
    }

    /**
     * 标记为已分析
     */
    public void markAsAnalyzed(Long id, Long analysisResultId) {
        SlowLog slowLog = new SlowLog();
        slowLog.setId(id);
        slowLog.setAnalyzed(1);
        slowLog.setAnalysisResultId(analysisResultId);
        slowLogMapper.updateById(slowLog);
    }

    /**
     * 获取未分析的慢查询
     */
    public List<SlowLog> getUnanalyzedSlowLogs(Long dataSourceId) {
        LambdaQueryWrapper<SlowLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SlowLog::getAnalyzed, 0);
        wrapper.eq(dataSourceId != null, SlowLog::getDataSourceId, dataSourceId);
        wrapper.orderByDesc(SlowLog::getExecutionTime);
        return slowLogMapper.selectList(wrapper);
    }

    /**
     * 统计慢查询数量
     */
    public long countSlowLogs(Long dataSourceId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SlowLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dataSourceId != null, SlowLog::getDataSourceId, dataSourceId);
        wrapper.ge(startTime != null, SlowLog::getQueryTime, startTime);
        wrapper.le(endTime != null, SlowLog::getQueryTime, endTime);
        return slowLogMapper.selectCount(wrapper);
    }
}