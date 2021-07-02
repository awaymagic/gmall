package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * Date:2021/7/2
 * Author:away
 * Description:
 */
@Data
public class SearchResponseAttrValueVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValues;

}
