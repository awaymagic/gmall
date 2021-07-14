package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * Date:2021/7/11
 * Author:away
 * Description:商品详情分组
 */
@Data
public class GroupVo {
    private String name;
    private List<AttrValueVo> attrs;
}
