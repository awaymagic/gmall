package com.atguigu.gmall.geteway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Date:2021/6/23
 * Author:away
 * Description: 允许跨域访问设置
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        //允许跨域的域名
        config.addAllowedOrigin("http://manager.gmall.com");
        config.addAllowedOrigin("http://localhost:1024");
        config.addAllowedOrigin("http://www.gmall.com");
        config.addAllowedOrigin("http://gmall.com");
        //允许访问的方式
        config.addAllowedMethod("*");
        //允许携带的头信息
        config.addAllowedHeader("*");
        //允许携带cookie,如果允许则origin不能写*
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(configurationSource);
    }
}
