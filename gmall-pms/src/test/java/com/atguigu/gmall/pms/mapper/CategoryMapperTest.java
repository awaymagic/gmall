package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date:2021/7/7
 * Author:away
 * Description:
 */
@SpringBootTest
class CategoryMapperTest {

    @Resource
    private CategoryMapper categoryMapper;

    @Test
    void queryLvl2WithSubsByPid() {
        this.categoryMapper.queryLvl2WithSubsByPid(1l).forEach(System.out::println);
    }
}