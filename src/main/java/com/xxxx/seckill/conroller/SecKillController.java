package com.xxxx.seckill.conroller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxxx.seckill.config.AccessLimit;
import com.xxxx.seckill.pojo.*;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillGoodsService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.util.JsonUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.OrderDetailVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender sender;

    //用于内存标记，标记当前商品是否有库存
    private Map<Long, Boolean> emptyStockMap = new HashMap<>();


    /**
     * 优化前QPS ： 244
     * 前后端分离优化后QPS : 256
     * redis优化后： 1744
     * */
    @RequestMapping(value = "/{path}/doSeckill",method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId) {
//        判断用户是否登录，如果没登录则返回登录页
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations ops = redisTemplate.opsForValue();

        boolean check = orderService.checkPath(path,user,goodsId);
        if (!check) {
            return RespBean.error(RespBeanEnum.STATUS_ILLEGAL);
        }

        //根据id查询商品  当前代码只和redis打交道，不做任何数据库操作
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);

        //检查重复购买让操作通过redis而不是通过数据库
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //检查内存标记，减少对redis的无意义访问
        if (!emptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存
        Long stock = ops.decrement("seckillGoods:" + goodsId);
        if (stock < 0) {
            emptyStockMap.put(goodsId, false);
            //如果库存是0，那么减完会变成-1 所以要给加回成0
            ops.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //创建个对象，用于向mq发送用户和商品，用于下单
        SeckillMessage message = new SeckillMessage(user, goodsId);
        //将对象转成json串放入mq
        sender.sendSeckillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success();
    }

    //实现InitializingBean接口，重写方法，当系统启动，启动流程加载玩配置文件之后会自动执行这个方法
    //在系统初始化的时候，读取数据库秒杀商品，将商品库存放入redis中
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //,1, TimeUnit.DAYS) 理论上讲应该加上商品秒杀时间的有效期
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            //通过内存标记，当前商品是否有库存，减少无意义的对redis进行访问
            emptyStockMap.put(goodsVo.getId(), true);
        });

    }

    /**
     * @return orderId: 成功  -1 秒杀失败，0 排队中
     * */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user,Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user,goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 获取秒杀地址
     *
     * @param goodsId 商品ID
     * @return 秒杀真实地址
     */

    @AccessLimit(second=5,maxCount=5)
    @RequestMapping("/path")
    @ResponseBody
    public RespBean getPath(User user, Long goodsId) {
        String path = orderService.createPath(user,goodsId);
        return RespBean.success(path);
    }
}
