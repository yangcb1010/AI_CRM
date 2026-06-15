package com.kakarote.ai_crm.entity.BO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductUpdateBO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;
    private String productCode;
    private String mainImage;
    private Long categoryId;
    private String productType;
    private String unit;
    private BigDecimal standardPrice;
    private BigDecimal costPrice;
    private Long ownerId;
    private String description;
    private Map<String, Object> customFields;
}
