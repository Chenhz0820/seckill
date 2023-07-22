package com.xxxx.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxxx.seckill.pojo.SeckillGoods;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 *
 *
 *
 * @author chen
 *
 */
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    List<SeckillGoods> findSeckillGoods();

}
