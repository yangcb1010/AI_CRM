package com.kakarote.ai_crm.common.log;

import com.kakarote.ai_crm.mapper.AccessLogMapper;
import com.kakarote.ai_crm.mapper.ErrorLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class AccessLogCleanupScheduler {

    @Autowired
    private AccessLogMapper accessLogMapper;

    @Autowired
    private ErrorLogMapper errorLogMapper;

    @Value("${crm.access-log.retention-days:30}")
    private int retentionDays;

    @Value("${crm.access-log.cleanup-batch-size:1000}")
    private int cleanupBatchSize;

    @Scheduled(cron = "${crm.access-log.cleanup-cron:0 30 3 * * ?}")
    public void cleanupExpiredLogs() {
        if (retentionDays <= 0 || cleanupBatchSize <= 0) {
            return;
        }

        Date cutoff = Date.from(LocalDateTime.now()
                .minusDays(retentionDays)
                .atZone(ZoneId.systemDefault())
                .toInstant());
        try {
            int errorDeleted = errorLogMapper.deleteExpiredBefore(cutoff, cleanupBatchSize);
            int accessDeleted = accessLogMapper.deleteExpiredBefore(cutoff, cleanupBatchSize);
            if (errorDeleted > 0 || accessDeleted > 0) {
                log.info("Cleaned expired access logs: access={}, errors={}, cutoff={}",
                        accessDeleted, errorDeleted, cutoff);
            }
        } catch (Exception e) {
            log.warn("Cleanup expired access logs failed: cutoff={}, error={}",
                    cutoff, e.getMessage(), e);
        }
    }
}
