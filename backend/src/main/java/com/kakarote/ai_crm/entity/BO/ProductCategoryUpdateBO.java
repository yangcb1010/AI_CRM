package com.kakarote.ai_crm.entity.BO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductCategoryUpdateBO {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private Integer sortOrder;
}
