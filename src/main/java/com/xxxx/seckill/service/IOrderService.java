package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.OrderDetailVo;


/**
 * <p>
 *  服务类
 * </p>
 *
 *
 *
 *
 * @author chen
 *
 */
public interface IOrderService extends IService<Order> {

	/**
	 * 功能描述: 秒杀
	 *
	 * @param:
	 * @return:
	 *
	 *
	 *
	 * @since: 1.0.0
	 * @Author:chen
	 */
	Order seckill(User user, GoodsVo goods);


    OrderDetailVo detail(Long orderId);

    String createPath(User user, Long goodsId);

	boolean checkPath(String path,User user, Long goodsId);
}
