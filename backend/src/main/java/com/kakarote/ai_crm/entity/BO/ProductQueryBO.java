package com.kakarote.ai_crm.entity.BO;

import com.kakarote.ai_crm.common.PageEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductQueryBO extends PageEntity {

    private String keyword;
    private Long categoryId;
    private Boolean includeChildCategory;
    private String productType;
    private String status;
    private Long ownerId;
}
