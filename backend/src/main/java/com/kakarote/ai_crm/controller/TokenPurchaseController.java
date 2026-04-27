package com.kakarote.ai_crm.controller;

import com.kakarote.ai_crm.common.result.Result;
import com.kakarote.ai_crm.entity.BO.TokenPurchaseCreateBO;
import com.kakarote.ai_crm.entity.VO.TokenPurchaseOptionVO;
import com.kakarote.ai_crm.entity.VO.TokenPurchaseOrderVO;
import com.kakarote.ai_crm.service.ITokenPurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tokenPurchase")
public class TokenPurchaseController {

    private final ITokenPurchaseService tokenPurchaseService;

    /**
     * 初始化Token 购买实例。
     */
    public TokenPurchaseController(ITokenPurchaseService tokenPurchaseService) {
        this.tokenPurchaseService = tokenPurchaseService;
    }

    /**
     * 获取选项。
     */
    @GetMapping("/options")
    public Result<TokenPurchaseOptionVO> getOptions() {
        return Result.ok(tokenPurchaseService.getOptions());
    }

    /**
     * 创建订单。
     */
    @PostMapping("/orders")
    public Result<TokenPurchaseOrderVO> createOrder(@Valid @RequestBody TokenPurchaseCreateBO createBO) {
        return Result.ok(tokenPurchaseService.createOrder(createBO));
    }

    /**
     * 获取订单。
     */
    @GetMapping("/orders/{orderNo}")
    public Result<TokenPurchaseOrderVO> getOrder(@PathVariable String orderNo) {
        return Result.ok(tokenPurchaseService.getOrder(orderNo));
    }

    /**
     * 查询RecentOrders。
     */
    @GetMapping("/orders")
    public Result<List<TokenPurchaseOrderVO>> listRecentOrders(
            @RequestParam(name = "limit", defaultValue = "10") Integer limit) {
        return Result.ok(tokenPurchaseService.listRecentOrders(limit != null ? limit : 10));
    }

    /**
     * 处理微信回调。
     */
    @PostMapping(value = "/notify/wechat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleWechatNotify(@RequestBody String body) {
        return ResponseEntity.ok(tokenPurchaseService.handleWechatNotify(body));
    }

    /**
     * 处理支付宝回调。
     */
    @PostMapping(value = "/notify/alipay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleAlipayNotify(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(tokenPurchaseService.handleAlipayNotify(params));
    }
}
