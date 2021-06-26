package com.atguigu.gmall.pms.feign;


import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;


/**
 * Date:2021/6/26
 * Author:away
 * Description:
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
