package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.auth.AuthException;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Date:2021/7/14
 * Author:away
 * Description:
 */
@Service
@EnableConfigurationProperties(JwtProperties.class) // 启用标记为Spring组件
public class AuthService {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties properties;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 1. 调用ums接口校验登录名和密码（查询）
        ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();

        // 2.判断用户信息是否为空
        if (userEntity == null) {
            throw new AuthException("登录名或者密码错误！请重新输入");
        }

        // 3.组装载荷
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userEntity.getId());
        map.put("username", userEntity.getUsername());

        // 4.防止jwt的盗用，加入登录用户的ip地址
        String ip = IpUtils.getIpAddressAtService(request);
        map.put("ip", ip);

        // 5.制作jwt
        String token = JwtUtils.generateToken(map, this.properties.getPrivateKey(), this.properties.getExpire());

        // 6.把jwt类型的token放入cookie中
        CookieUtils.setCookie(request, response, this.properties.getCookieName(), token, this.properties.getExpire() * 60);

        // 7.把昵称放入cookie
        CookieUtils.setCookie(request, response, this.properties.getUnick(), userEntity.getNickname(), this.properties.getExpire() * 60);
    }
}
