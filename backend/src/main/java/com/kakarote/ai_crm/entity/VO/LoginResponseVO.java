package com.kakarote.ai_crm.entity.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "登录响应")
public class LoginResponseVO {

    @Schema(description = "登录令牌")
    private String token;

    @Schema(description = "当前登录用户")
    private ManageUserVO userInfo;

    @Schema(description = "是否需要选择企业")
    private Boolean requiresTenantSelection = Boolean.FALSE;

    @Schema(description = "可选企业列表")
    private List<LoginTenantOptionVO> tenantOptions;
}
