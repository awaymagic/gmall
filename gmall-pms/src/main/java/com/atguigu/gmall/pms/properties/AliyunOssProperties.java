package com.atguigu.gmall.pms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Date:2021/6/24
 * Author:away
 * Description:
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ali.oss")
public class AliyunOssProperties {
    private String accessId; // 请填写您的AccessKeyId。
    private String accessKey; // 请填写您的AccessKeySecret。
    private String endpoint; // 请填写您的 endpoint。
    private String bucket; // 请填写您的 bucketname 。
}
