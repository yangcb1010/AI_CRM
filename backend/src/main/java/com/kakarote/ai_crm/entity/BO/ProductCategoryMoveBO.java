package com.kakarote.ai_crm.entity.BO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductCategoryMoveBO {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long parentId;
    private Integer sortOrder;
}
