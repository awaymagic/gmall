package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Date:2021/7/1
 * Author:away
 * Description:
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
