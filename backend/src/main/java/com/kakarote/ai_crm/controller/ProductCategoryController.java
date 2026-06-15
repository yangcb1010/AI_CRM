package com.kakarote.ai_crm.controller;

import com.kakarote.ai_crm.common.result.Result;
import com.kakarote.ai_crm.entity.BO.ProductCategoryAddBO;
import com.kakarote.ai_crm.entity.BO.ProductCategoryMoveBO;
import com.kakarote.ai_crm.entity.BO.ProductCategoryUpdateBO;
import com.kakarote.ai_crm.entity.VO.ProductCategoryVO;
import com.kakarote.ai_crm.service.IProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product/category")
@Tag(name = "Product category")
public class ProductCategoryController {

    private final IProductCategoryService productCategoryService;

    public ProductCategoryController(IProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping("/tree")
    @Operation(summary = "Product category tree")
    public Result<List<ProductCategoryVO>> tree() {
        return Result.ok(productCategoryService.tree());
    }

    @PostMapping("/add")
    @Operation(summary = "Create product category")
    public Result<Long> addCategory(@Valid @RequestBody ProductCategoryAddBO bo) {
        return Result.ok(productCategoryService.addCategory(bo));
    }

    @PostMapping("/update")
    @Operation(summary = "Update product category")
    public Result<Void> updateCategory(@Valid @RequestBody ProductCategoryUpdateBO bo) {
        productCategoryService.updateCategory(bo);
        return Result.ok();
    }

    @PostMapping("/move")
    @Operation(summary = "Move product category")
    public Result<Void> moveCategory(@Valid @RequestBody ProductCategoryMoveBO bo) {
        productCategoryService.moveCategory(bo);
        return Result.ok();
    }

    @PostMapping("/delete/{categoryId}")
    @Operation(summary = "Delete product category")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        productCategoryService.deleteCategory(categoryId);
        return Result.ok();
    }
}
