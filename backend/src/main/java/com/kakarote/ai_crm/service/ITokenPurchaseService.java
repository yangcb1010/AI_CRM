package com.kakarote.ai_crm.service;

import com.kakarote.ai_crm.entity.BO.TokenPurchaseCreateBO;
import com.kakarote.ai_crm.entity.VO.TokenPurchaseOptionVO;
import com.kakarote.ai_crm.entity.VO.TokenPurchaseOrderVO;

import java.util.List;
import java.util.Map;

public interface ITokenPurchaseService {

    /**
     * 获取选项。
     */
    TokenPurchaseOptionVO getOptions();

    /**
     * 创建订单。
     */
    TokenPurchaseOrderVO createOrder(TokenPurchaseCreateBO createBO);

    /**
     * 获取订单。
     */
    TokenPurchaseOrderVO getOrder(String orderNo);

    /**
     * 查询RecentOrders。
     */
    List<TokenPurchaseOrderVO> listRecentOrders(int limit);

    /**
     * 处理微信回调。
     */
    String handleWechatNotify(String body);

    /**
     * 处理支付宝回调。
     */
    String handleAlipayNotify(Map<String, String> params);
}
