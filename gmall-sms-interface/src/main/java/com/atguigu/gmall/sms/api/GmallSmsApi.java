package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Date:2021/6/26
 * Author:away
 * Description:
 */

public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sales/save")
    public ResponseVo saleSales(@RequestBody SkuSaleVo saleVo);
}
