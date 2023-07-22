package com.xxxx.seckill.service.impl;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.mapper.OrderMapper;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.SeckillGoods;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillGoodsService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.util.MD5Util;
import com.xxxx.seckill.util.UUIDUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.OrderDetailVo;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

	@Autowired
	private ISeckillGoodsService seckillGoodsService;
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private ISeckillOrderService seckillOrderService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 功能描述: 秒杀下数据库订单
	 */
	@Transactional
	@Override
	public Order seckill(User user, GoodsVo goods) {
		//1 减库存
		SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId()));
//		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
		//updateById会对所有的字段进行update影响效率
//		seckillGoodsService.updateById(seckillGoods);

		//update seckillgoods set stock_count=stock_count-1 where goods_id=goods.getId() stock_count>0
		boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().
				setSql("stock_count=stock_count-1").eq("goods_id", goods.getId()).gt("stock_count",0));

		if (seckillGoods.getStockCount() < 1) {
		//判断如果当数据库没有库存时，设定一个库存为null的redi skey
			redisTemplate.opsForValue().set("isStockEmpty:"+goods.getId(),"0");
			return null;
		}
		//2 生成订单
		Order order = new Order();
		order.setUserId(user.getId());
		order.setGoodsId(goods.getId());
		order.setDeliverAddrId(0L);
		order.setGoodsName(goods.getGoodsName());
		order.setGoodsCount(1);
		order.setGoodsPrice(seckillGoods.getSeckillPrice());
		order.setOrderChannel(1);
		order.setStatus(0);
		order.setCreateDate(new Date());
		orderMapper.insert(order);
		//3 生成秒杀订单
		SeckillOrder seckillOrder = new SeckillOrder();
		seckillOrder.setUserId(user.getId());
		seckillOrder.setOrderId(order.getId());
		seckillOrder.setGoodsId(goods.getId());
		seckillOrderService.save(seckillOrder); //要明确，为什么oder用mapper保存，而这用sevice，为了业务隔离
		//将购买记录添加到redis中，这样的判断重复购买就可以直接从redis中取而不走数据库
		redisTemplate.opsForValue().set("order:"+user.getId()+":"+goods.getId(),seckillOrder);
		return order;
	}

	@Override
	public OrderDetailVo detail(Long orderId) {
		if (orderId == null) {
			throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
		}
		Order order = orderMapper.selectById(orderId);
		GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
		OrderDetailVo orderDetailVo = new OrderDetailVo();
		orderDetailVo.setOrder(order);
		orderDetailVo.setGoodsVo(goodsVo);
		return orderDetailVo;
	}

	@Override
	public String createPath(User user, Long goodsId) {
		String path = MD5Util.md5(UUIDUtil.uuid() + "abcd");
		System.out.println(user);
		redisTemplate.opsForValue().set("seckillPath:"+user.getId()+":"+goodsId,path,1, TimeUnit.MINUTES);
		return path;
	}

	@Override
	public boolean checkPath(String path,User user, Long goodsId) {
		if (user == null || goodsId < 0 || !StringUtils.hasLength(path)) {
			return false;
		}
		String check = (String)redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
		return path.equals(check);
	}


}
