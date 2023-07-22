package com.xxxx.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxxx.seckill.pojo.Goods;
import com.xxxx.seckill.vo.GoodsVo;

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
public interface GoodsMapper extends BaseMapper<Goods> {

	/**
	 * 功能描述: 获取商品列表
	 *
	 * @param:
	 * @return:
	 *
	 *
	 *
	 * @since: 1.0.0
	 * @Author:chen
	 */
	List<GoodsVo> findGoodsVo();

	/**
	 * 功能描述: 获取商品详情
	 *
	 * @param:
	 * @return:
	 *
	 *
	 *
	 * @since: 1.0.0
	 * @Author:chen
	 * @param goodsId
	 */
	GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
