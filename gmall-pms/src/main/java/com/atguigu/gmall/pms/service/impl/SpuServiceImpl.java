package com.atguigu.gmall.pms.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),//第一个参数要求为IPage对象
                new QueryWrapper<SpuEntity>()//第二个参数为查询条件
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByCidAndKeywordAndPage(PageParamVo paramVo, Long categoryId) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        // 如果categoryId不为0，需要按要求查询本类
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }
        // 关键字
        String key = paramVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            /**
             * SELECT id,name,category_id,brand_id,publish_status,create_time,update_time
             * FROM pms_spu
             * WHERE (category_id = ? AND (id = ? OR name LIKE ?))
             * LIMIT ?,?
             *
             * wrapper后面直接写条件，条件之间的默认是and关系，并且没有括号。
             * t为函数式接口 在此为wrapper自己，把wrapper的条件放在括号里代表是小括号。or也是同理使用
             */
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
        }
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );
        return new PageResultVo(page);
    }

}