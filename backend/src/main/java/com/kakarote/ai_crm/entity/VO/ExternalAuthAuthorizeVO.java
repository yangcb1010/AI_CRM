package com.kakarote.ai_crm.entity.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAuthAuthorizeVO {

    private String provider;

    private String authorizeUrl;
}
