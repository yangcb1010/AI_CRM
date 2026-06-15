package com.kakarote.ai_crm.controller;

import com.kakarote.ai_crm.common.BasePage;
import com.kakarote.ai_crm.common.result.Result;
import com.kakarote.ai_crm.entity.BO.ProductAddBO;
import com.kakarote.ai_crm.entity.BO.ProductQueryBO;
import com.kakarote.ai_crm.entity.BO.ProductStatusUpdateBO;
import com.kakarote.ai_crm.entity.BO.ProductTransferBO;
import com.kakarote.ai_crm.entity.BO.ProductUpdateBO;
import com.kakarote.ai_crm.entity.VO.ProductVO;
import com.kakarote.ai_crm.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
@Tag(name = "Product")
public class ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/add")
    @Operation(summary = "Create product")
    public Result<Long> addProduct(@Valid @RequestBody ProductAddBO bo) {
        return Result.ok(productService.addProduct(bo));
    }

    @PostMapping("/update")
    @Operation(summary = "Update product")
    public Result<Void> updateProduct(@Valid @RequestBody ProductUpdateBO bo) {
        productService.updateProduct(bo);
        return Result.ok();
    }

    @PostMapping("/delete/{productId}")
    @Operation(summary = "Delete product")
    public Result<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return Result.ok();
    }

    @PostMapping("/status")
    @Operation(summary = "Update product status")
    public Result<Void> updateStatus(@Valid @RequestBody ProductStatusUpdateBO bo) {
        productService.updateStatus(bo);
        return Result.ok();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer product owner")
    public Result<Void> transferProducts(@Valid @RequestBody ProductTransferBO bo) {
        productService.transferProducts(bo);
        return Result.ok();
    }

    @PostMapping("/queryPageList")
    @Operation(summary = "Query product page")
    public Result<BasePage<ProductVO>> queryPageList(@RequestBody ProductQueryBO queryBO) {
        return Result.ok(productService.queryPageList(queryBO));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Product detail")
    public Result<ProductVO> detail(@PathVariable Long productId) {
        return Result.ok(productService.getProductDetail(productId));
    }
}
