package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * Date:2021/7/2
 * Author:away
 * Description:
 */
@Data
public class SearchResponseVo {
    // 品牌列表
    private List<BrandEntity> brands;

    // 分类列表
    private List<CategoryEntity> categories;

    // 规格参数过滤
    private List<SearchResponseAttrValueVo> filters;

    // 分页参数
    private Long total; // 总记录数
    private Integer pageNum;
    private Integer pageSize;

    private List<Goods> goodsList;
}
