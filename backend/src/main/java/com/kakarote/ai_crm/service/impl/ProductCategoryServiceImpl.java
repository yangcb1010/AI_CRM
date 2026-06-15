package com.kakarote.ai_crm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kakarote.ai_crm.common.exception.BusinessException;
import com.kakarote.ai_crm.common.result.SystemCodeEnum;
import com.kakarote.ai_crm.entity.BO.ProductCategoryAddBO;
import com.kakarote.ai_crm.entity.BO.ProductCategoryMoveBO;
import com.kakarote.ai_crm.entity.BO.ProductCategoryUpdateBO;
import com.kakarote.ai_crm.entity.PO.ProductCategory;
import com.kakarote.ai_crm.entity.VO.ProductCategoryVO;
import com.kakarote.ai_crm.mapper.ProductCategoryMapper;
import com.kakarote.ai_crm.mapper.ProductMapper;
import com.kakarote.ai_crm.service.IProductCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory>
        implements IProductCategoryService {

    private static final String DEFAULT_CATEGORY_NAME = "未分类";
    private static final int MAX_LEVEL = 3;

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;

    public ProductCategoryServiceImpl(ProductCategoryMapper productCategoryMapper,
                                      ProductMapper productMapper) {
        this.productCategoryMapper = productCategoryMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long ensureDefaultCategoryId() {
        ProductCategory existing = productCategoryMapper.selectOne(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getParentId, 0L)
                .eq(ProductCategory::getCategoryName, DEFAULT_CATEGORY_NAME)
                .eq(ProductCategory::getDelFlag, 0)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing.getCategoryId();
        }

        ProductCategory category = new ProductCategory();
        category.setParentId(0L);
        category.setCategoryName(DEFAULT_CATEGORY_NAME);
        category.setCategoryPath(DEFAULT_CATEGORY_NAME);
        category.setLevel(1);
        category.setSortOrder(0);
        category.setStatus(1);
        category.setDelFlag(0);
        productCategoryMapper.insert(category);
        return category.getCategoryId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCategory(ProductCategoryAddBO bo) {
        String name = normalizeName(bo.getCategoryName());
        Long parentId = bo.getParentId() == null ? 0L : bo.getParentId();
        int level = 1;
        String path = name;
        if (parentId > 0) {
            ProductCategory parent = getActiveCategory(parentId);
            level = parent.getLevel() + 1;
            if (level > MAX_LEVEL) {
                throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Product category supports at most 3 levels");
            }
            path = parent.getCategoryPath() + "/" + name;
        }
        assertCategoryNameUnique(parentId, name, null);

        ProductCategory category = new ProductCategory();
        category.setParentId(parentId);
        category.setCategoryName(name);
        category.setCategoryPath(path);
        category.setLevel(level);
        category.setSortOrder(bo.getSortOrder() == null ? nextSortOrder(parentId) : bo.getSortOrder());
        category.setStatus(1);
        category.setDelFlag(0);
        productCategoryMapper.insert(category);
        return category.getCategoryId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(ProductCategoryUpdateBO bo) {
        ProductCategory category = getActiveCategory(bo.getCategoryId());
        String name = normalizeName(bo.getCategoryName());
        assertCategoryNameUnique(category.getParentId(), name, category.getCategoryId());
        category.setCategoryName(name);
        category.setSortOrder(bo.getSortOrder() == null ? category.getSortOrder() : bo.getSortOrder());
        category.setCategoryPath(buildCategoryPath(category.getParentId(), name));
        productCategoryMapper.updateById(category);
        refreshChildrenPath(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveCategory(ProductCategoryMoveBO bo) {
        ProductCategory category = getActiveCategory(bo.getCategoryId());
        Long parentId = bo.getParentId() == null ? 0L : bo.getParentId();
        if (Objects.equals(category.getCategoryId(), parentId)) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Cannot move category under itself");
        }
        int level = 1;
        if (parentId > 0) {
            ProductCategory parent = getActiveCategory(parentId);
            level = parent.getLevel() + 1;
            if (level > MAX_LEVEL || maxChildDepth(category.getCategoryId()) + level - 1 > MAX_LEVEL) {
                throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Product category supports at most 3 levels");
            }
        }
        assertCategoryNameUnique(parentId, category.getCategoryName(), category.getCategoryId());
        category.setParentId(parentId);
        category.setLevel(level);
        category.setSortOrder(bo.getSortOrder() == null ? category.getSortOrder() : bo.getSortOrder());
        category.setCategoryPath(buildCategoryPath(parentId, category.getCategoryName()));
        productCategoryMapper.updateById(category);
        refreshChildrenPath(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        ProductCategory category = getActiveCategory(categoryId);
        if (isDefaultCategory(category)) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Default category cannot be deleted");
        }
        Long childCount = productCategoryMapper.selectCount(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getParentId, categoryId)
                .eq(ProductCategory::getDelFlag, 0));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Category has children");
        }
        Long productCount = productMapper.countByCategoryId(categoryId);
        if (productCount != null && productCount > 0) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Category has products");
        }
        category.setDelFlag(1);
        productCategoryMapper.updateById(category);
    }

    @Override
    public List<ProductCategoryVO> tree() {
        ensureDefaultCategoryId();
        List<ProductCategory> categories = productCategoryMapper.selectList(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getDelFlag, 0)
                .orderByAsc(ProductCategory::getLevel)
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getCreateTime));
        Map<Long, ProductCategoryVO> map = new LinkedHashMap<>();
        List<ProductCategoryVO> roots = new ArrayList<>();
        for (ProductCategory category : categories) {
            ProductCategoryVO vo = BeanUtil.copyProperties(category, ProductCategoryVO.class);
            map.put(category.getCategoryId(), vo);
        }
        for (ProductCategory category : categories) {
            ProductCategoryVO vo = map.get(category.getCategoryId());
            if (category.getParentId() == null || category.getParentId() == 0) {
                roots.add(vo);
            } else {
                ProductCategoryVO parent = map.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    @Override
    public Long findCategoryIdByPath(String categoryPath) {
        if (StrUtil.isBlank(categoryPath)) {
            return null;
        }
        String normalized = categoryPath.trim().replace("\\", "/").replaceAll("/+", "/");
        ProductCategory category = productCategoryMapper.selectOne(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getCategoryPath, normalized)
                .eq(ProductCategory::getDelFlag, 0)
                .last("LIMIT 1"));
        return category == null ? null : category.getCategoryId();
    }

    private ProductCategory getActiveCategory(Long categoryId) {
        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category == null || Integer.valueOf(1).equals(category.getDelFlag())) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Product category does not exist");
        }
        return category;
    }

    private String normalizeName(String name) {
        if (StrUtil.isBlank(name)) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Category name is required");
        }
        return name.trim();
    }

    private void assertCategoryNameUnique(Long parentId, String name, Long excludeId) {
        LambdaQueryWrapper<ProductCategory> wrapper = Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getParentId, parentId == null ? 0L : parentId)
                .eq(ProductCategory::getCategoryName, name)
                .eq(ProductCategory::getDelFlag, 0);
        if (excludeId != null) {
            wrapper.ne(ProductCategory::getCategoryId, excludeId);
        }
        Long count = productCategoryMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(SystemCodeEnum.SYSTEM_NO_VALID, "Category name already exists");
        }
    }

    private int nextSortOrder(Long parentId) {
        ProductCategory latest = productCategoryMapper.selectOne(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getParentId, parentId == null ? 0L : parentId)
                .eq(ProductCategory::getDelFlag, 0)
                .orderByDesc(ProductCategory::getSortOrder)
                .last("LIMIT 1"));
        return latest == null || latest.getSortOrder() == null ? 10 : latest.getSortOrder() + 10;
    }

    private String buildCategoryPath(Long parentId, String categoryName) {
        if (parentId == null || parentId == 0) {
            return categoryName;
        }
        ProductCategory parent = getActiveCategory(parentId);
        return parent.getCategoryPath() + "/" + categoryName;
    }

    private void refreshChildrenPath(ProductCategory parent) {
        List<ProductCategory> children = productCategoryMapper.selectList(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getParentId, parent.getCategoryId())
                .eq(ProductCategory::getDelFlag, 0));
        for (ProductCategory child : children) {
            child.setLevel(parent.getLevel() + 1);
            child.setCategoryPath(parent.getCategoryPath() + "/" + child.getCategoryName());
            productCategoryMapper.updateById(child);
            refreshChildrenPath(child);
        }
    }

    private int maxChildDepth(Long categoryId) {
        List<ProductCategory> all = productCategoryMapper.selectList(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getDelFlag, 0));
        return maxChildDepth(categoryId, all);
    }

    private int maxChildDepth(Long categoryId, List<ProductCategory> all) {
        return all.stream()
                .filter(item -> Objects.equals(item.getParentId(), categoryId))
                .map(child -> 1 + maxChildDepth(child.getCategoryId(), all))
                .max(Comparator.naturalOrder())
                .orElse(1);
    }

    private boolean isDefaultCategory(ProductCategory category) {
        return category != null
                && Objects.equals(category.getParentId(), 0L)
                && DEFAULT_CATEGORY_NAME.equals(category.getCategoryName());
    }
}
