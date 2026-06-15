package com.kakarote.ai_crm.entity.BO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExternalAuthTicketLoginBO {

    @NotBlank(message = "ticket is required")
    private String ticket;
}
