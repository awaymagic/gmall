package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Date:2021/7/12
 * Author:away
 * Description:
 */
@Controller
public class ItemController {

    @Resource
    private ItemService itemService;

    @GetMapping("{skuId}.html")
    public String loadData(@PathVariable("skuId") Long skuId, Model model) {
        ItemVo itemVo = this.itemService.loadData(skuId);
        model.addAttribute("itemVo", itemVo);

        this.itemService.asyncExecute(itemVo);
        return "item";
    }
}
