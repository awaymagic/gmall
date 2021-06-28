package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author away
 * @email awaymagic@gmail.com
 * @date 2021-06-22 18:54:21
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpuByCidAndKeywordAndPage(PageParamVo paramVo, Long categoryId);

    void bigSave(SpuVo spu);

}

