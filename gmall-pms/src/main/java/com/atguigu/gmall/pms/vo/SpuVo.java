package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * Date:2021/6/25
 * Author:away
 * Description: 接收商品保存信息的实体类
 */
@Data
public class SpuVo extends SpuEntity {
    private List<String> spuImages;
    private List<SpuAttrValueVo> baseAttrs;
    private List<SkuVo> skus;
}
