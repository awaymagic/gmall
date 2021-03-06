package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Date:2021/7/11
 * Author:away
 * Description:
 */
@Data
public class ItemVo {
    //面包屑
    // 一二级分类
    private List<CategoryEntity> categories;
    // 品牌信息
    private Long brandId;
    private String brandName;
    // spu信息
    private Long spuId;
    private String spuName;

    //中间详细信息
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImage;
    // 营销信息
    private List<ItemSaleVo> sales;
    // 是否有货
    private Boolean store;
    // sku的图片列表
    private List<SkuImagesEntity> images;

    //销售属性列表
    private List<SaleAttrValueVo> saleAttrs;

    // 当前sku的销售属性
    private Map<Long, String> saleAttr;

    // 为了页面跳转，需要销售属性组合与skuId的映射关系 V
    private String skuJsons;

    // 商品描述 V
    private List<String> spuImages;

    // 规格参数分组列表
    private List<GroupVo> groups;

}
