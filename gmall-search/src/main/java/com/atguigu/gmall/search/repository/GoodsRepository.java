package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Date:2021/7/1
 * Author:away
 * Description:
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {

}
