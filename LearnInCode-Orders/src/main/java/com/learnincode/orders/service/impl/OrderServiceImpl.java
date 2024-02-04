package com.learnincode.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnincode.orders.mapper.OrdersGoodsMapper;
import com.learnincode.orders.mapper.OrdersMapper;
import com.learnincode.orders.model.dto.CreateOrderDto;
import com.learnincode.orders.model.dto.PayRecordDto;
import com.learnincode.orders.model.po.Orders;
import com.learnincode.orders.model.po.OrdersGoods;
import com.learnincode.orders.service.OrderService;
import com.learnincode.orders.utils.IdWorkerUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    OrdersMapper ordersMapper;

    @Autowired
    OrdersGoodsMapper goodsMapper;

    @Override
    public PayRecordDto generatePayCode(String userId, CreateOrderDto createOrderDto) {

        return null;
    }

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, CreateOrderDto createOrderDto) {

        //添加商品订单
        Orders orders = saveOrders(userId, createOrderDto);

        //添加支付交易记录


        //生成二维码

        return null;
    }


    @Transactional
    public Orders saveOrders(String userId,  CreateOrderDto createOrderDto)
    {
        // 幂等性处理（订单表有out_business_id唯一主键约束）
        String outBusinessId = createOrderDto.getOutBusinessId();

        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getOutBusinessId, outBusinessId));

        if(order != null) return order;

        order = new Orders();

        // =======================插入订单表=======================

        //  雪花算法工具类生成订单号
        long orderId = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setTotalPrice(createOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("600001");      //订单状态设置为未支付
        order.setUserId(userId);
        order.setOrderType(createOrderDto.getOrderType());  // 支付类型，比如购买课程，一对一咨询
        order.setOrderName(createOrderDto.getOrderName());  // 订单名称
        String orderDetail = createOrderDto.getOrderDetail();
        order.setOrderDetail(orderDetail);  // 订单明细
        order.setOrderDescrip(createOrderDto.getOrderDescrip());    // 订单描述
        order.setOutBusinessId(createOrderDto.getOutBusinessId());//选课记录id
        ordersMapper.insert(order);

        // ======================= 插入订单明细表 =======================
        // 获取订单id
        Long id = order.getId();

        List<OrdersGoods> ordersGoods = JSON.parseArray(orderDetail, OrdersGoods.class);
        // 给每一个订单明细拷贝订单
        for (OrdersGoods ordersGood : ordersGoods) {
            BeanUtils.copyProperties(order,ordersGood);
            ordersGood.setOrderId(id); // 设置明细对应的订单id
            goodsMapper.insert(ordersGood); // 插入到明细表
        }

        return order;
    }


}
