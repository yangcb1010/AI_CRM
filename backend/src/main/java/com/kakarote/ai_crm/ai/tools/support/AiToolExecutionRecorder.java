package com.kakarote.ai_crm.ai.tools.support;

import cn.hutool.core.util.StrUtil;
import com.kakarote.ai_crm.ai.context.AiContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AiToolExecutionRecorder {

    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<ToolExecution>> EXECUTIONS =
            new ConcurrentHashMap<>();

    public void begin(Long sessionId) {
        if (sessionId != null) {
            EXECUTIONS.put(sessionId, new CopyOnWriteArrayList<>());
        }
    }

    public void record(String toolName, String methodName, Object result, Throwable throwable) {
        Long sessionId = AiContextHolder.getCurrentSessionId();
        if (sessionId == null) {
            return;
        }
        EXECUTIONS.computeIfAbsent(sessionId, ignored -> new CopyOnWriteArrayList<>())
                .add(new ToolExecution(
                        toolName,
                        methodName,
                        result instanceof String text ? StrUtil.trim(text) : null,
                        resolveErrorReason(throwable)
                ));
    }

    public ToolExecution getLatestFailure(Long sessionId) {
        List<ToolExecution> executions = sessionId == null
                ? List.of()
                : EXECUTIONS.getOrDefault(sessionId, new CopyOnWriteArrayList<>());
        for (int i = executions.size() - 1; i >= 0; i--) {
            ToolExecution execution = executions.get(i);
            if (execution.failed()) {
                return execution;
            }
        }
        return null;
    }

    public void finish(Long sessionId) {
        if (sessionId != null) {
            EXECUTIONS.remove(sessionId);
        }
    }

    private String resolveErrorReason(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return StrUtil.blankToDefault(throwable.getMessage(), throwable.getClass().getSimpleName());
    }

    public record ToolExecution(
            String toolName,
            String methodName,
            String resultText,
            String errorReason
    ) {
        public boolean failed() {
            return StrUtil.isNotBlank(errorReason);
        }
    }
}
