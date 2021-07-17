package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Date:2021/7/12
 * Author:away
 * Description:
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
