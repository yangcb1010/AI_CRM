package com.kakarote.ai_crm.entity.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CustomerLogoUploadVO", description = "Customer logo upload result")
public class CustomerLogoUploadVO {

    @Schema(description = "Stored logo path")
    private String logo;

    @Schema(description = "Logo access URL")
    private String logoUrl;
}
