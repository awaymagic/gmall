package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Resource
    private SpuDescService descService;
    @Resource
    private SpuAttrValueService spuAttrValueService;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SkuAttrValueService skuAttrValueService;
    @Resource
    private GmallSmsClient smsClient;

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

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spu) {

        //1.保存spu相关的3张表
        //1.1保存pms_spu
        Long spuId = saveSpuInfo(spu);
        //1.2保存pms_spu_desc
//        saveSpuDesc(spu, spuId);
        this.descService.saveSpuDesc(spu, spuId);
//        int i = 1 / 0;

        //1.3保存pms_spu_attr_value
        saveBaseAttrs(spu, spuId);
        //2.保存sku相关的3张表
        saveSkuInfo(spu, spuId);
    }

    private void saveSkuInfo(SpuVo spu, Long spuId) {
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(skuVo -> {
            //2.1保存pms_sku
            skuVo.setSpuId(spuId);
            skuVo.setCategoryId(spu.getCategoryId());
            skuVo.setBrandId(spu.getBrandId());
            //  获取页面的图片列表
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                //  把第一张作为默认图片
                skuVo.setDefaultImage(StringUtils.isBlank(skuVo.getDefaultImage()) ? images.get(0) : skuVo.getDefaultImage());
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();
            //2.2保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)) {
                this.skuImagesService.saveBatch(images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    //如果当前图片的地址和sku的默认图片地址相同，则设置为1，否则为0
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(skuVo.getDefaultImage(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList()));
            }
            //2.3保存pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(skuAttrValueEntity -> {
                    skuAttrValueEntity.setSkuId(skuId);
                });
                this.skuAttrValueService.saveBatch(saleAttrs);
            }
            //3.保存营销信息相关的3张表
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.smsClient.saleSales(skuSaleVo);
        });
    }

    private void saveBaseAttrs(SpuVo spu, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            //把SpuAttrValueVo集合转为SpuAttrValueEntity集合
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().filter(spuAttrValueVo ->
                    spuAttrValueVo.getAttrValue() != null
            ).map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                return spuAttrValueEntity;
            }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }


    private Long saveSpuInfo(SpuVo spu) {
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        return spu.getId();
    }
}