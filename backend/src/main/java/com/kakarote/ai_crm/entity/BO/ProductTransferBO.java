package com.kakarote.ai_crm.entity.BO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductTransferBO {

    private List<Long> productIds;

    @NotNull(message = "Owner is required")
    private Long ownerId;
}
