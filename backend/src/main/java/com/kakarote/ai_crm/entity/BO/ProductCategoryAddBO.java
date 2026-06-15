package com.kakarote.ai_crm.entity.BO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductCategoryAddBO {

    private Long parentId;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private Integer sortOrder;
}
