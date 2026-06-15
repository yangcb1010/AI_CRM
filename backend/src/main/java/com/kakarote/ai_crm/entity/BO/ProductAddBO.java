package com.kakarote.ai_crm.entity.BO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductAddBO {

    @NotBlank(message = "Product name is required")
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
