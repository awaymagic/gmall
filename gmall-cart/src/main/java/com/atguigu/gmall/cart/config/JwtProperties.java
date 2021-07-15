package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * Date:2021/7/14
 * Author:away
 * Description:
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String pubFilePath;
    private String cookieName;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            this.publicKey = RsaUtils.getPublicKey(pubFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
