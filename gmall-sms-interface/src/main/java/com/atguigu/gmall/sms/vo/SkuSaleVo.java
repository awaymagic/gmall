package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Date:2021/6/26
 * Author:away
 * Description:
 */
@Data
public class SkuSaleVo {
    private Long skuId;
    //积分优惠字段
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;//前端传来的是集合

    //打折信息
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    //满减信息
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;
}
