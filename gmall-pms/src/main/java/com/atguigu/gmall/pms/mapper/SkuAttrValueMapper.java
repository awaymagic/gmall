package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author away
 * @email awaymagic@gmail.com
 * @date 2021-06-22 18:54:21
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {


    List<Map<String, Object>> queryMappingBySpuId(@Param("skuIds") List<Long> skuIds);// 集合要起别名，不然任意参数名字收不到
}
