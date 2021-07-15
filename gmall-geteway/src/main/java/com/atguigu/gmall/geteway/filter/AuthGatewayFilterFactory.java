package com.atguigu.gmall.geteway.filter;

import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.geteway.config.JwtProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Date:2021/7/14
 * Author:away
 * Description: 自定义拦截器 拦截特定服务的请求
 *              实现GatewayFilterFactory接口，推荐继承AbstractGatewayFilterFactory抽象类
 */
@EnableConfigurationProperties(JwtProperties.class)
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Autowired
    private JwtProperties properties;

    // 重写父类的无参构造方法，调用super(PathConfig.class)
    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    // 重写shortcutFieldOrder方法，指定接受参数的字段顺序
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    // 重写shortcutType方法，返回ShortcutType.GATHER_LIST，指定接收类型（因为配置保存在了集合 GATHER_LIST可以接收多个字段
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                //System.out.println("我是局部过滤器，我只拦截经过特定路由的请求。paths = " + config.paths);

                // 获取请求对象 ServerHttpRequest --> HttpServletRequest
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                // 获取当前请求的路径
                String curPath = request.getURI().getPath();
                // 获取拦截路径名单
                List<String> paths = config.paths;
                // 1.判断当前请求 路径在不在拦截名单中，allMatch都不在则直接放行(有在的则拦截)
                if (paths.stream().allMatch(path -> !curPath.startsWith(path))) {
                    return chain.filter(exchange);
                }

                // 2.获取token信息，异步：token头中  同步：cookie中
                String token = request.getHeaders().getFirst(properties.getToken());
                if (StringUtils.isBlank(token)) {
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    HttpCookie cookie = cookies.getFirst(properties.getCookieName());
                    token = cookie.getValue();
                }

                // 3.判断token是否为空，为空则拦截并重定向到登录页面
                if (StringUtils.isBlank(token)) {
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    // 被拦截
                    return response.setComplete();
                }

                try {
                    // 4.解析jwt类型的token，如果解析过程中出现异常，则拦截并重定向到登录页面
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, properties.getPublicKey());

                    // 5.获取载荷中的ip(登录用户) 和 当前请求的ip地址（当前用户） 判断是否一致，如果不一致说明是盗用的，则重定向到登录页面
                    String ip = map.get("ip").toString(); // 载荷中登录用户的ip地址
                    String curIp = IpUtils.getIpAddressAtGateway(request); // 当前用户的ip地址
                    if (!StringUtils.equals(ip, curIp)) {
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        // 被拦截
                        return response.setComplete();
                    }

                    // 6.把解析后的用户信息传递给后续服务:request.mutate()把请求转换 .build()创建一个新的request对象
                    request.mutate()
                            .header("userId", map.get("userId").toString())
                            .header("username", map.get("username").toString())
                            .build();
                     // request在exchange中,把新request放入传给后继服务
                    exchange.mutate().request(request).build();

                    // 7.放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    // 被拦截
                    return response.setComplete();
                }
            }
        };
    }

    @Data
    public static class PathConfig {
        // 网关中配置的参数 个数不固定可以用集合
        private List<String> paths;
    }
}
