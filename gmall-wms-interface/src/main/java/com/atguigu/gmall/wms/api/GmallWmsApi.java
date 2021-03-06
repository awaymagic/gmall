package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Date:2021/7/1
 * Author:away
 * Description:
 */
public interface GmallWmsApi {
    @GetMapping("wms/waresku/sku/{skuId}")
    @ApiOperation("获取某个sku的库存信息")
    public ResponseVo<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId") Long skuId);
}
