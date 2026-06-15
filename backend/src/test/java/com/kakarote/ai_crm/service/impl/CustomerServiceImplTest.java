package com.kakarote.ai_crm.service.impl;

import com.kakarote.ai_crm.entity.VO.CustomerLogoUploadVO;
import com.kakarote.ai_crm.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerServiceImplTest {

    @Test
    void uploadCustomerLogoStoresImageUnderCustomerLogoPath() {
        CustomerServiceImpl service = new CustomerServiceImpl();
        FileStorageService fileStorageService = mock(FileStorageService.class);
        ReflectionTestUtils.setField(service, "fileStorageService", fileStorageService);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "acme.logo.png",
            "image/png",
            new byte[] {1, 2, 3}
        );
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        when(fileStorageService.upload(eq(file), pathCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(1));
        when(fileStorageService.getUrl(anyString())).thenReturn("/uploads/customer-logo.png");

        CustomerLogoUploadVO result = service.uploadCustomerLogo(file);

        assertThat(result.getLogo()).startsWith("customer/logo/").endsWith(".png");
        assertThat(result.getLogoUrl()).isEqualTo("/uploads/customer-logo.png");
    }
}
