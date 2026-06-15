package com.kakarote.ai_crm.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeAnswerLocalizationUtilTest {

    @Test
    void localizeToChineseReplacesCommonEnglishKnowledgeLabels() {
        String answer = """
                ## Summary

                According to the retrieved information: the customer needs renewal.

                **References:** contract.pdf
                """;

        String localized = KnowledgeAnswerLocalizationUtil.localizeToChinese(answer);

        assertThat(localized).contains("## 摘要");
        assertThat(localized).contains("根据检索信息");
        assertThat(localized).contains("**参考文件**：");
        assertThat(localized).doesNotContain("Summary");
        assertThat(localized).doesNotContain("retrieved information");
        assertThat(localized).doesNotContain("References");
    }
}
