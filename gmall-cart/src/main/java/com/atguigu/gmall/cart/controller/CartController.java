package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * Date:2021/7/16
 * Author:away
 * Description:
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * 添加购物车成功，重定向到购物车成功页
     * @param cart
     * @return
     */
    @GetMapping
    public String addCart(Cart cart) {

        this.cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId() + "&count=" + cart.getCount();
    }

    /**
     * 跳转到添加成功页
     *
     * @param cart
     * @param model
     * @return
     */
    @GetMapping("addCart.html")
    public String queryCartBySkuId(Cart cart, Model model) {
        BigDecimal count = cart.getCount();
        cart = this.cartService.queryCartBySkuId(cart);
        cart.setCount(count);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    @GetMapping("cart.html")
    public String queryCarts(Model model){
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts", carts);
        return "cart";
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId")Long skuId){
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    @GetMapping("test")
    @ResponseBody
    public String test(){
        System.out.println(loginInterceptor.getUserInfo());

        return "hello cart!";
    }

}
