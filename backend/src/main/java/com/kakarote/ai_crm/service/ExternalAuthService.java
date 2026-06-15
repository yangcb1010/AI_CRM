package com.kakarote.ai_crm.service;

import com.kakarote.ai_crm.entity.BO.ExternalAuthTicketLoginBO;
import com.kakarote.ai_crm.entity.VO.ExternalAuthAuthorizeVO;
import com.kakarote.ai_crm.entity.VO.ExternalAuthBindingVO;
import com.kakarote.ai_crm.entity.VO.ExternalAuthProviderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

public interface ExternalAuthService {

    List<ExternalAuthProviderVO> listProviders();

    ExternalAuthAuthorizeVO createAuthorizeUrl(String provider, String redirect, HttpServletRequest request);

    ExternalAuthAuthorizeVO createBindAuthorizeUrl(String provider, String redirect, HttpServletRequest request);

    String handleCallback(String provider, String code, String state, String error, HttpServletRequest request);

    Map<String, Object> loginByTicket(ExternalAuthTicketLoginBO loginBO, HttpServletResponse response);

    List<ExternalAuthBindingVO> listBindings();

    void unbind(String provider);
}
