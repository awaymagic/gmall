package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Date:2021/7/2
 * Author:away
 * Description:
 */
@Controller
@RequestMapping("search")
public class SearchController {
    @Resource
    private SearchService searchService;

    @GetMapping
    public String search(SearchParamVo paramVo, Model model) {
        SearchResponseVo responseVo = this.searchService.search(paramVo);
        model.addAttribute("response", responseVo);
        model.addAttribute("searchParam", paramVo);
        return "search";
    }
}
