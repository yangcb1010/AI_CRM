package com.kakarote.ai_crm.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kakarote.ai_crm.entity.PO.CrmTenant;
import com.kakarote.ai_crm.mapper.CrmTenantMapper;
import com.kakarote.ai_crm.service.ICrmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrmTenantServiceImpl extends ServiceImpl<CrmTenantMapper, CrmTenant> implements ICrmTenantService {

    /**
     * 获取赠送TokenTotal。
     */
    @Override
    public long getGiftTokenTotal(Long tenantId) {
        CrmTenant tenant = tenantId != null ? getById(tenantId) : null;
        if (tenant == null) {
            return 0L;
        }
        Long total = tenant.getGiftTokenTotal();
        return total != null && total > 0 ? total : DEFAULT_GIFT_TOKEN_TOTAL;
    }

    /**
     * 获取赠送TokenUsed。
     */
    @Override
    public long getGiftTokenUsed(Long tenantId) {
        CrmTenant tenant = tenantId != null ? getById(tenantId) : null;
        if (tenant == null || tenant.getGiftTokenUsed() == null) {
            return 0L;
        }
        return Math.max(tenant.getGiftTokenUsed(), 0L);
    }

    /**
     * 获取赠送Token剩余。
     */
    @Override
    public long getGiftTokenRemaining(Long tenantId) {
        long total = getGiftTokenTotal(tenantId);
        long used = getGiftTokenUsed(tenantId);
        return Math.max(total - used, 0L);
    }

    /**
     * 判断是否存在可用赠送Tokens。
     */
    @Override
    public boolean hasAvailableGiftTokens(Long tenantId) {
        return getGiftTokenRemaining(tenantId) > 0;
    }

    /**
     * 获取PurchasedTokenTotal。
     */
    @Override
    public long getPurchasedTokenTotal(Long tenantId) {
        CrmTenant tenant = tenantId != null ? getById(tenantId) : null;
        if (tenant == null || tenant.getPurchasedTokenTotal() == null) {
            return 0L;
        }
        return Math.max(tenant.getPurchasedTokenTotal(), 0L);
    }

    /**
     * 获取PurchasedTokenUsed。
     */
    @Override
    public long getPurchasedTokenUsed(Long tenantId) {
        CrmTenant tenant = tenantId != null ? getById(tenantId) : null;
        if (tenant == null || tenant.getPurchasedTokenUsed() == null) {
            return 0L;
        }
        return Math.max(tenant.getPurchasedTokenUsed(), 0L);
    }

    /**
     * 获取PurchasedToken剩余。
     */
    @Override
    public long getPurchasedTokenRemaining(Long tenantId) {
        long total = getPurchasedTokenTotal(tenantId);
        long used = getPurchasedTokenUsed(tenantId);
        return Math.max(total - used, 0L);
    }

    /**
     * 获取TotalToken剩余。
     */
    @Override
    public long getTotalTokenRemaining(Long tenantId) {
        return getGiftTokenRemaining(tenantId) + getPurchasedTokenRemaining(tenantId);
    }

    /**
     * 判断是否存在可用Tokens。
     */
    @Override
    public boolean hasAvailableTokens(Long tenantId) {
        return getTotalTokenRemaining(tenantId) > 0;
    }

    /**
     * 消耗赠送Tokens。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeGiftTokens(Long tenantId, long consumeTokens) {
        if (tenantId == null || consumeTokens <= 0) {
            return;
        }
        baseMapper.consumeGiftTokens(tenantId, consumeTokens);
    }

    /**
     * 消耗Tokens。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeTokens(Long tenantId, long consumeTokens) {
        if (tenantId == null || consumeTokens <= 0) {
            return;
        }

        CrmTenant tenant = baseMapper.selectByIdForUpdate(tenantId);
        if (tenant == null) {
            return;
        }

        long giftTotal = ObjectUtil.defaultIfNull(tenant.getGiftTokenTotal(), DEFAULT_GIFT_TOKEN_TOTAL);
        long giftUsed = Math.max(ObjectUtil.defaultIfNull(tenant.getGiftTokenUsed(), 0L), 0L);
        long giftRemaining = Math.max(giftTotal - giftUsed, 0L);

        long purchasedTotal = Math.max(ObjectUtil.defaultIfNull(tenant.getPurchasedTokenTotal(), 0L), 0L);
        long purchasedUsed = Math.max(ObjectUtil.defaultIfNull(tenant.getPurchasedTokenUsed(), 0L), 0L);
        long purchasedRemaining = Math.max(purchasedTotal - purchasedUsed, 0L);

        long consumeGift = Math.min(giftRemaining, consumeTokens);
        long consumePurchased = Math.min(purchasedRemaining, Math.max(consumeTokens - consumeGift, 0L));

        if (consumeGift <= 0 && consumePurchased <= 0) {
            return;
        }

        tenant.setGiftTokenUsed(giftUsed + consumeGift);
        tenant.setPurchasedTokenUsed(purchasedUsed + consumePurchased);
        updateById(tenant);
    }

    /**
     * 新增PurchasedTokens。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPurchasedTokens(Long tenantId, long tokenAmount) {
        if (tenantId == null || tokenAmount <= 0) {
            return;
        }
        baseMapper.addPurchasedTokens(tenantId, tokenAmount);
    }
}
