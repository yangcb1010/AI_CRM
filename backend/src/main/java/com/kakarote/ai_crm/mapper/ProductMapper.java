package com.kakarote.ai_crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kakarote.ai_crm.common.BasePage;
import com.kakarote.ai_crm.entity.BO.ProductQueryBO;
import com.kakarote.ai_crm.entity.PO.Product;
import com.kakarote.ai_crm.entity.VO.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    BasePage<ProductVO> queryPageList(IPage<ProductVO> page, @Param("query") ProductQueryBO query);

    ProductVO getProductById(@Param("productId") Long productId);

    @Select("""
            SELECT COUNT(1)
            FROM crm_product
            WHERE product_code = #{productCode}
              AND del_flag = 0
              AND product_id <> COALESCE(CAST(#{excludeProductId,jdbcType=BIGINT} AS BIGINT), -1)
            """)
    Long countByCode(@Param("productCode") String productCode,
                     @Param("excludeProductId") Long excludeProductId);

    @Select("""
            SELECT COUNT(1)
            FROM crm_product
            WHERE category_id = #{categoryId}
              AND del_flag = 0
            """)
    Long countByCategoryId(@Param("categoryId") Long categoryId);
}
