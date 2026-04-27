package com.kakarote.ai_crm.ai;

import cn.hutool.core.util.StrUtil;

/**
 * AI 运行模式：
 * gift   - 使用平台赠送额度与默认模型
 * custom - 使用租户自定义模型配置
 */
public enum AiMode {

    GIFT("gift"),
    CUSTOM("custom");

    private final String code;

    AiMode(String code) {
        this.code = code;
    }

    /**
     * 获取验证码。
     */
    public String getCode() {
        return code;
    }

    /**
     * 解析AI模式。
     */
    public static AiMode resolve(String value) {
        if (StrUtil.equalsIgnoreCase(CUSTOM.code, value)) {
            return CUSTOM;
        }
        return GIFT;
    }
}
