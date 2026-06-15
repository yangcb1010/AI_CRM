package com.kakarote.ai_crm.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CrmNoopTools {

    @Tool(description = "Use this when the user asks a CRM-related question that does not require creating, updating, deleting, or querying business data with another tool.")
    public String directAnswer(
            @ToolParam(description = "Brief reason why no data operation is required", required = false) String reason) {
        return reason == null || reason.isBlank()
                ? "No CRM data operation is required. Answer the user directly."
                : "No CRM data operation is required: " + reason.trim();
    }
}
