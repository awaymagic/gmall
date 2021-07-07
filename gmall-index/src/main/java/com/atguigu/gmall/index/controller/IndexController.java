package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Date:2021/7/7
 * Author:away
 * Description:
 */
@Controller
public class IndexController {
    @Resource
    private IndexService indexService;

    @GetMapping
    public String toIndex(Model model) {
        List<CategoryEntity> categoryEntityList = this.indexService.queryLv1Categories();
        model.addAttribute("categories", categoryEntityList);
        return "index";
    }

    @GetMapping("index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLvl2WithSubsByPid(@PathVariable("pid") Long pid) {
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl2WithSubsByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }
}
