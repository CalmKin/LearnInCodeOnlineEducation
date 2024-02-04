package com.learnincode.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.orders.mapper.OrdersGoodsMapper;
import com.learnincode.orders.mapper.OrdersMapper;
import com.learnincode.orders.mapper.PayRecordMapper;
import com.learnincode.orders.model.dto.CreateOrderDto;
import com.learnincode.orders.model.dto.PayRecordDto;
import com.learnincode.orders.model.po.Orders;
import com.learnincode.orders.model.po.OrdersGoods;
import com.learnincode.orders.model.po.PayRecord;
import com.learnincode.orders.service.OrderService;
import com.learnincode.orders.utils.IdWorkerUtils;
import com.learnincode.orders.utils.QRCodeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Value("${pay.qrcodeurl}")  // 需要生成二维码的url
    String qrcodeurl;

    @Autowired
    OrdersMapper ordersMapper;

    @Autowired
    OrdersGoodsMapper goodsMapper;

    @Autowired
    PayRecordMapper payRecordMapper;

    @Override
    public PayRecordDto generatePayCode(String userId, CreateOrderDto createOrderDto) {

        return null;
    }

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, CreateOrderDto createOrderDto) {

        //添加商品订单
        Orders orders = saveOrders(userId, createOrderDto);

        if(orders.getStatus().equals("600002"))
        {
            throw new BusinessException("订单已支付");
        }

        //添加支付交易记录
        PayRecord payRecord = createPayRecord(orders);

        //根据支付记录id,生成二维码
        String qrCode = null;
        QRCodeUtil qrCodeUtil = new QRCodeUtil();

        // 将支付id填入url中，发送给支付宝
        try {
            String url  = String.format(qrcodeurl, payRecord.getPayNo());
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (Exception e) {
            throw new BusinessException("生成二维码失败");
        }

        // 将生成的二维码包装在支付记录,返回给前端
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }




    public PayRecord createPayRecord(Orders orders){
        
        if(orders==null){
            throw new BusinessException("订单不存在");
        }
        // 业务幂等性，防止重复插入支付记录
        if(orders.getStatus().equals("600002")){
            throw new BusinessException("订单已支付");
        }
        PayRecord payRecord = new PayRecord();

        //生成支付交易流水号
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orders.getId());  //支付对应的订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");  //支付状态未支付
        payRecord.setUserId(orders.getUserId());

        // 插入支付记录
        payRecordMapper.insert(payRecord);
        return payRecord;

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
        int insert = ordersMapper.insert(order);
        if(insert <=0 )
        {
            throw new BusinessException("保存订单失败");
        }


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
