package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Date:2021/6/25
 * Author:away
 * Description:接收valueSelected的扩展实体类
 */
public class SpuAttrValueVo extends SpuAttrValueEntity {
    private List<String> valueSelected;

    public void setValueSelected(List<String> valueSelected) {
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }
        //把接收过来的List集合转成String字符串，并用逗号隔开存到数据库
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
