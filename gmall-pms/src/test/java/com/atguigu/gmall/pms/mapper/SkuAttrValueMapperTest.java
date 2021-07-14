package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date:2021/7/12
 * Author:away
 * Description:测试用例
 */
@SpringBootTest
class SkuAttrValueMapperTest {

    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;

    @Test
    void queryMappingBySpuId() {
        System.out.println(this.skuAttrValueMapper.queryMappingBySpuId(Arrays.asList(11l, 12l, 13l, 14l)));

    }
}