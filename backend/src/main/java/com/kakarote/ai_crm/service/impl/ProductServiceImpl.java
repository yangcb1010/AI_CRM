package com.kakarote.ai_crm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kakarote.ai_crm.common.BasePage;
import com.kakarote.ai_crm.common.exception.BusinessException;
import com.kakarote.ai_crm.common.result.SystemCodeEnum;
import com.kakarote.ai_crm.entity.BO.ProductAddBO;
import com.kakarote.ai_crm.entity.BO.ProductQueryBO;
import com.kakarote.ai_crm.entity.BO.ProductStatusUpdateBO;
import com.kakarote.ai_crm.entity.BO.ProductTransferBO;
import com.kakarote.ai_crm.entity.BO.ProductUpdateBO;
import com.kakarote.ai_crm.entity.PO.ManagerUser;
import com.kakarote.ai_crm.entity.PO.Product;
import com.kakarote.ai_crm.entity.PO.ProductCategory;
import com.kakarote.ai_crm.entity.VO.ProductVO;
import com.kakarote.ai_crm.mapper.ManageUserMapper;
import com.kakarote.ai_crm.mapper.ProductMapper;
import com.kakarote.ai_crm.service.IProductCategoryService;
import com.kakarote.ai_crm.service.IProductService;
import com.kakarote.ai_crm.utils.UserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_INACTIVE = "inactive";
    private static final String DEFAULT_PRODUCT_TYPE = "goods";

    private final ProductMapper productMapper;
    private final IProductCategoryService productCategoryService;
    private final ManageUserMapper manageUserMapper;

    public ProductServiceImpl(ProductMapper productMapper,
                              IProductCategoryService productCategoryService,
                              ManageUserMapper manageUserMapper) {
        this.productMapper = productMapper;
        this.productCategoryService = productCategoryService;
        this.manageUserMapper = manageUserMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addProduct(ProductAddBO bo) {
        Product product = new Product();
        product.setProductName(requireProductName(bo.getProductName()));
        product.setProductCode(normalizeOptional(bo.getProductCode()));
        product.setMainImage(normalizeOptional(bo.getMainImage()));
        validateCodeForSave(product.getProductCode(), null);
        product.setCategoryId(resolveCategoryId(bo.getCategoryId()));
        product.setProductType(StrUtil.blankToDefault(normalizeOptional(bo.getProductType()), DEFAULT_PRODUCT_TYPE));
        product.setUnit(normalizeOptional(bo.getUnit()));
        product.setStandardPrice(bo.getStandardPrice());
        product.setCostPrice(bo.getCostPrice());
        product.setOwnerId(resolveOwnerId(bo.getOwnerId()));
        product.setStatus(STATUS_ACTIVE);
        product.setDescription(normalizeOptional(bo.getDescription()));
        product.setDelFlag(0);
        Long currentUserId = UserUtil.getUserId();
        product.setCreateUserId(currentUserId);
        product.setUpdateUserId(currentUserId);
        productMapper.insert(product);
        return product.getProductId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductUpdateBO bo) {
        Product product = getVisibleProduct(bo.getProductId());
        if (bo.getProductName() != null) {
            product.setProductName(requireProductName(bo.getProductName()));
        }
        if (bo.getProductCode() != null) {
            product.setProductCode(normalizeOptional(bo.getProductCode()));
        }
        if (bo.getMainImage() != null) {
            product.setMainImage(normalizeOptional(bo.getMainImage()));
        }
        validateCodeForSave(product.getProductCode(), product.getProductId());
        if (bo.getCategoryId() != null) {
            product.setCategoryId(resolveCategoryId(bo.getCategoryId()));
        }
        if (bo.getProductType() != null) {
            product.setProductType(StrUtil.blankToDefault(normalizeOptional(bo.getProductType()), DEFAULT_PRODUCT_TYPE));
        }
        if (bo.getUnit() != null) {
            product.setUnit(normalizeOptional(bo.getUnit()));
        }
        if (bo.getStandardPrice() != null) {
            product.setStandardPrice(bo.getStandardPrice());
        }
        if (bo.getCostPrice() != null) {
            product.setCostPrice(bo.getCostPrice());
        }
        if (bo.getOwnerId() != null) {
            product.setOwnerId(resolveOwnerId(bo.getOwnerId()));
        }
        if (bo.getDescription() != null) {
            product.setDescription(normalizeOptional(bo.getDescription()));
        }
        product.setUpdateUserId(UserUtil.getUserId());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long productId) {
        Product product = getVisibleProduct(productId);
        product.setDelFlag(1);
        product.setUpdateUserId(UserUtil.getUserId());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(ProductStatusUpdateBO bo) {
        Product product = getVisibleProduct(bo.getProductId());
        product.setStatus(normalizeStatus(bo.getStatus()));
        product.setUpdateUserId(UserUtil.getUserId());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferProducts(ProductTransferBO bo) {
        if (bo.getProductIds() == null || bo.getProductIds().isEmpty()) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "No products selected");
        }
        Long ownerId = resolveOwnerId(bo.getOwnerId());
        for (Long productId : bo.getProductIds()) {
            Product product = getVisibleProduct(productId);
            product.setOwnerId(ownerId);
            product.setUpdateUserId(UserUtil.getUserId());
            productMapper.updateById(product);
        }
    }

    @Override
    public BasePage<ProductVO> queryPageList(ProductQueryBO queryBO) {
        ProductQueryBO safeQuery = queryBO == null ? new ProductQueryBO() : queryBO;
        return productMapper.queryPageList(safeQuery.parse(), safeQuery);
    }

    @Override
    public ProductVO getProductDetail(Long productId) {
        ProductVO vo = productMapper.getProductById(productId);
        if (vo == null) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Product does not exist");
        }
        return vo;
    }

    @Override
    public Product getVisibleProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || Integer.valueOf(1).equals(product.getDelFlag())) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Product does not exist");
        }
        return product;
    }

    @Override
    public Product findVisibleProductByCode(String productCode) {
        String code = normalizeOptional(productCode);
        if (code == null) {
            return null;
        }
        return productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getProductCode, code)
                .eq(Product::getDelFlag, 0)
                .last("LIMIT 1"));
    }

    private String requireProductName(String productName) {
        if (StrUtil.isBlank(productName)) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Product name is required");
        }
        return productName.trim();
    }

    private String normalizeOptional(String value) {
        if (StrUtil.isBlank(value) || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }

    private void validateCodeForSave(String productCode, Long excludeProductId) {
        if (StrUtil.isBlank(productCode)) {
            return;
        }
        Long count = productMapper.countByCode(productCode, excludeProductId);
        if (count != null && count > 0) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Product code already exists");
        }
    }

    private Long resolveCategoryId(Long categoryId) {
        Long resolved = categoryId == null ? productCategoryService.ensureDefaultCategoryId() : categoryId;
        ProductCategory category = productCategoryService.getById(resolved);
        if (category == null || Integer.valueOf(1).equals(category.getDelFlag())) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Product category does not exist");
        }
        if (category.getLevel() != null && category.getLevel() > 3) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Product category supports at most 3 levels");
        }
        return resolved;
    }

    private Long resolveOwnerId(Long ownerId) {
        Long resolved = ownerId == null ? UserUtil.getUserId() : ownerId;
        ManagerUser owner = manageUserMapper.getUserId(resolved);
        if (owner == null || Integer.valueOf(0).equals(owner.getStatus())) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Owner does not exist");
        }
        return resolved;
    }

    private String normalizeStatus(String status) {
        String normalized = normalizeOptional(status);
        if (STATUS_ACTIVE.equalsIgnoreCase(normalized)) {
            return STATUS_ACTIVE;
        }
        if (STATUS_INACTIVE.equalsIgnoreCase(normalized)) {
            return STATUS_INACTIVE;
        }
        throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Invalid product status");
    }
}
