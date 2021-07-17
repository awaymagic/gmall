package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Date:2021/7/16
 * Author:away
 * Description:
 */
@Service
public class CartService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartAsyncService asyncService;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    public void addCart(Cart cart) {
        // 1.获取登录信息
        String userId = getUserId();

        // 2.获取redis中该用户的购物车(hash的内层map）
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        // 3.判断该用户的购物车信息是否已包含了该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount(); // 用户添加购物的商品数量
        if (boundHashOps.hasKey(skuId)) {
            // 4.包含，更新数量
            String cartJson = boundHashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);// 反序列化，json字符串转为对象
            cart.setCount(cart.getCount().add(count)); // 添加购物车数量
            // 数据更新redis和mysql
            boundHashOps.put(skuId, JSON.toJSONString(cart));
            this.asyncService.updateCart(userId, skuId, cart);
        } else {
            // 5.不包含，给该用户新增购物车记录 skuId count

            // 根据skuId查询sku
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new CartException("您加入购物车的商品不存在");
            }
            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());
            cart.setDefaultImage(skuEntity.getDefaultImage());

            cart.setUserId(userId);

            // 根据skuId查询营销信息
            ResponseVo<List<ItemSaleVo>> itemSaleVoResposneVo = this.smsClient.queryItemSalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = itemSaleVoResposneVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            // 根据skuId查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuattrValueResponseVo = this.pmsClient.querySkuAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuattrValueResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            // 根据skuId查询库存信息
            ResponseVo<List<WareSkuEntity>> listResponseVo = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            // 商品刚加入购物车时，默认为选中状态
            cart.setCheck(true);

            // 写入数据库
            this.asyncService.insertCart(userId.toString(),cart);
            // 添加实时价格缓存
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        boundHashOps.put(skuId,JSON.toJSONString(cart));
    }

    public Cart queryCartBySkuId(Cart cart) {
        // 获取登录信息
        String userId = this.getUserId();

        // 获取内层的map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!hashOps.hasKey(cart.getSkuId().toString())) {
            throw new CartException("当前用户没有对应的购物车记录！");
        }
        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        return JSON.parseObject(cartJson, Cart.class);
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserKey();
        if (userInfo.getUserId() != null) {
            userId = userInfo.getUserId().toString();
        }
        return userId;
    }


    public List<Cart> queryCarts() {
        // 1.获取userKey
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();

        // 2.以userKey为key获取未登录的购物车
        BoundHashOperations<String, Object, Object> unloginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userKey);
        List<Object> unloginCartJsons = unloginHashOps.values();
        List<Cart> unloginCarts = null;
        if (!CollectionUtils.isEmpty(unloginCartJsons)){
            unloginCarts = unloginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                // 设置实时价格
                // cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                String currentPriceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPriceString));
                return cart;
            }).collect(Collectors.toList());
        }

        // 3.获取userId，如果userId为空，则直接返回未登录的购物车
        Long userId = userInfo.getUserId();
        if (userId == null){
            return unloginCarts;
        }

        // 4.合并未登录的购物车 到 登录状态的购物车
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);// 获取redis购物车
        if (!CollectionUtils.isEmpty(unloginCarts)){
            unloginCarts.forEach(cart -> {
                String skuId = cart.getSkuId().toString();
                BigDecimal count = cart.getCount();
                if (loginHashOps.hasKey(skuId)){
                    // 获取登录状态购物车中的对应记录
                    String cartJson = loginHashOps.get(skuId).toString();
                    // 登录状态购物车的记录，反序列化
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    // 写入数据库
                    this.asyncService.updateCart(userId.toString(), skuId,cart);
                } else {
                    cart.setUserId(userId.toString());
                    this.asyncService.insertCart(userId.toString(),cart);
                }
                loginHashOps.put(skuId, JSON.toJSONString(cart));
            });

            // 5.删除未登录的购物车
            this.redisTemplate.delete(KEY_PREFIX + userKey);
            this.asyncService.deleteCartByUserId(userKey);
        }

        // 6.查询合并后的购物车，返回给页面
        List<Object> loginCartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(loginCartJsons)){
            return loginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String currentPriceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPriceString));
                return cart;
            }).collect(Collectors.toList());
        }

        return null;
    }

    public void updateNum(Cart cart) {
        String userId = this.getUserId();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        if (!hashOps.hasKey(cart.getSkuId().toString())){
            throw new CartException("您没有对应的购物车记录");
        }

        BigDecimal count = cart.getCount();

        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        cart = JSON.parseObject(cartJson, Cart.class);
        cart.setCount(count);

        hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        this.asyncService.updateCart(userId, cart.getSkuId().toString(),cart);
    }

    public void deleteCart(Long skuId) {
        String userId = this.getUserId();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        hashOps.delete(skuId.toString());
        this.asyncService.deleteCartByUserIdAndSkuId(userId, skuId);
    }
}
