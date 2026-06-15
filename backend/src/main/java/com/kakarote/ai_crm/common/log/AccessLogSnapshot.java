package com.kakarote.ai_crm.common.log;

public record AccessLogSnapshot(
        Long userId,
        String username,
        String method,
        String requestUri,
        String queryString,
        String requestHeaders,
        String requestBody,
        String responseBody,
        Integer statusCode,
        Integer businessCode,
        Boolean success,
        String ipAddress,
        String userAgent,
        String traceId,
        Long costMs,
        Boolean requestTruncated,
        Boolean responseTruncated,
        Boolean resultResponse,
        ErrorSnapshot systemError
) {
    public record ErrorSnapshot(
            String exceptionName,
            String errorMessage,
            String stackTrace
    ) {
    }
}
