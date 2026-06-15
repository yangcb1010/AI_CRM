package com.kakarote.ai_crm.entity.BO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStatusUpdateBO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Status is required")
    private String status;
}
