package com.kakarote.ai_crm.entity.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录企业选项")
public class LoginTenantOptionVO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "企业ID")
    private Long tenantId;

    @Schema(description = "企业名称")
    private String tenantName;

    @Schema(description = "当前企业下的用户姓名")
    private String realname;
}
