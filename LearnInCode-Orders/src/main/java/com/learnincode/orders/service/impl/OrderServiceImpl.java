package com.learnincode.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.messagesdk.model.po.MqMessage;
import com.learnincode.messagesdk.service.MqMessageService;
import com.learnincode.orders.config.AlipayConfig;
import com.learnincode.orders.config.PayNotifyConfig;
import com.learnincode.orders.mapper.OrdersGoodsMapper;
import com.learnincode.orders.mapper.OrdersMapper;
import com.learnincode.orders.mapper.PayRecordMapper;
import com.learnincode.orders.model.dto.CreateOrderDto;
import com.learnincode.orders.model.dto.PayRecordDto;
import com.learnincode.orders.model.dto.PayStatusDto;
import com.learnincode.orders.model.po.Orders;
import com.learnincode.orders.model.po.OrdersGoods;
import com.learnincode.orders.model.po.PayRecord;
import com.learnincode.orders.service.OrderService;
import com.learnincode.orders.utils.IdWorkerUtils;
import com.learnincode.orders.utils.QRCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Value("${pay.qrcodeurl}")  // 需要生成二维码的url
    String qrcodeurl;
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @Autowired
    OrdersMapper ordersMapper;

    @Autowired
    OrdersGoodsMapper goodsMapper;

    @Autowired
    PayRecordMapper payRecordMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, CreateOrderDto createOrderDto) {

        //添加商品订单
        Orders orders = saveOrders(userId, createOrderDto);

        if (orders.getStatus().equals("600002")) {
            throw new BusinessException("订单已支付");
        }

        //添加支付交易记录
        PayRecord payRecord = createPayRecord(orders);

        //根据支付记录id,生成二维码
        String qrCode = null;
        QRCodeUtil qrCodeUtil = new QRCodeUtil();

        // 将支付id填入url中，发送给支付宝
        try {
            String url = String.format(qrcodeurl, payRecord.getPayNo());
            // 根据请求支付的接口生成二维码
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

    /**
     * 根据支付流水号查询支付记录
     *
     * @param payNo
     * @return
     */
    @Override
    public PayRecord getPayRecordByPayno(String payNo) {
        return payRecordMapper.selectOne(new LambdaQueryWrapper<PayRecord>().eq(PayRecord::getPayNo, payNo));
    }

    /**
     * 主动查询支付结果，并保存支付状态
     * @param payNo
     * @return
     */
    @Override
    public PayRecordDto queryPayResult(String payNo) {
        // =====================健壮性判断=====================
        PayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            throw new BusinessException("请重新点击支付获取二维码");
        }

        // ===================== 业务幂等性判断=====================
        //支付状态
        String status = payRecord.getStatus();

        //如果支付成功直接返回
        PayRecordDto payRecordDto = new PayRecordDto();
        if ("601002".equals(status)) {
            BeanUtils.copyProperties(payRecord, payRecordDto);
            return payRecordDto;
        }

        // 向支付宝查询结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);

        //==================== 将支付状态保存到数据库====================

        // 保存支付结果,防止事务失效
        OrderService proxy = (OrderService) AopContext.currentProxy();
        proxy.saveAliPayStatus(payStatusDto);

        // 重新查询支付记录
        PayRecord ret = payRecordMapper.selectOne(new LambdaQueryWrapper<PayRecord>().eq(PayRecord::getPayNo, payNo));
        BeanUtils.copyProperties(ret, payRecordDto);

        return payRecordDto;
    }


    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 支付交易号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo) {

        //========发起http请求支付宝查询支付结果=============
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        // 根据系统生成的订单号,查询支付宝的支付记录
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());


        AlipayTradeQueryResponse response = null;
        try {
            // 发起请求
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                throw new BusinessException("请求支付查询查询失败");
            }
        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}", e.toString(), e);
            throw new BusinessException("请求支付查询查询失败");
        }

        //==========解析响应结果,获取关键字段=============
        String resultJson = response.getBody();
        //响应结果转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");

        //获取支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");

        // ======================== 保存支付结果========================
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_status(trade_status); // 订单状态
        payStatusDto.setTrade_no(trade_no); // 支付宝的订单号
        payStatusDto.setTotal_amount(total_amount); // 总金额
        payStatusDto.setOut_trade_no(payNo);    // 自己服务的订单号

        return payStatusDto;
    }

    /**
     * @param payStatusDto 支付宝返回的支付结果信息
     * @return void
     * @description 保存支付宝支付结果
     */
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto) {

        //================业务健壮性判断================
        // 获取订单的payNo(商户端)
        String payNo = payStatusDto.getOut_trade_no();

        // 健壮性判断: 查询支付订单是否存在
        PayRecord payRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<PayRecord>().eq(PayRecord::getPayNo, payNo));
        if (payRecord == null) throw new BusinessException("支付记录不存在");

        // 健壮性判断,查询支付记录关联的订单是否存在
        Long orderId = payRecord.getOrderId();
        Orders orders = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getId, orderId));
        if (orders == null) throw new BusinessException("关联订单不存在");

        //================幂等性判断================

        // 已经支付成功,后面操作无需进行
        if ("601002".equals(payRecord.getStatus())) {
            return;
        }

        // 如果支付宝回调支付成功
        String tradeStatus = payStatusDto.getTrade_status();
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            // ================ 订单校验 ================
            //支付金额变为分
            Float totalPrice = payRecord.getTotalPrice() * 100;     // 系统支付记录记录的总金额
            Float total_amount = Float.parseFloat(payStatusDto.getTotal_amount()) * 100;    // 支付宝返回的支付记录总金额
            //校验总金额是否一致
            if (!payStatusDto.getApp_id().equals(APP_ID) || totalPrice.intValue() != total_amount.intValue()) {
                //校验失败
                log.info("校验支付结果失败,支付记录:{},APP_ID:{},totalPrice:{}", payRecord.toString(), payStatusDto.getApp_id(), total_amount.intValue());
                throw new BusinessException("校验支付结果失败");
            }

            //================ 保存状态到支付记录表================

            log.debug("更新支付结果,支付交易流水号:{},支付结果:{}", payNo, tradeStatus);
            payRecord.setOutPayChannel("AliPay");   // 支付渠道
            payRecord.setPaySuccessTime(LocalDateTime.now());   // 支付成功时间
            payRecord.setTotalPrice(totalPrice);    // 总金额
            payRecord.setStatus("601002");  // 支付状态为支付成功

            // 根据payNo更新支付记录
            int update = payRecordMapper.update(payRecord,
                    new LambdaQueryWrapper<PayRecord>().eq(PayRecord::getPayNo,payNo));

            if (update <= 0) throw new BusinessException("更新支付记录状态失败");

            log.info("更新支付记录状态成功:{}", payRecord.toString());

            //================ 保存状态到订单表================
            orders.setStatus("600002");
            update = ordersMapper.updateById(orders);
            if(update <= 0 )
            {
                log.info("更新订单表状态失败,订单号:{}", orderId);
                throw new BusinessException("更新订单表状态失败");
            }
            log.info("更新订单表状态成功,订单号:{}", orderId);



            // =====================添加消息到本地消息表=====================
            // 先将消息持久化到数据库,参数1：支付结果通知类型，2: 业务id，3:业务类型
            MqMessage mqMessage =
                    mqMessageService.addMessage("payresult_notify",orders.getOutBusinessId(), orders.getOrderType(), null);
            notifyPayResult(mqMessage);
        }


    }


    /**
     * @author CalmKin
     * @description 持久化支付结果后，通过消息队列进行通知
     * @version 1.0
     * @date 2024/2/5 8:11
     */
    @Override
    public void notifyPayResult(MqMessage message) {
        //1、消息体，转json
        String msg = JSON.toJSONString(message);

        // 将自定义的消息转化成MQ发送的消息
        Message mqMsg = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))   //转化成字节流
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)  // 持久化
                .build();

        // 2.给消息设置全局唯一的消息ID，需要封装到CorrelationData中
        // 利用的是自定义消息表的自增主键保证消息id的唯一
        CorrelationData correlationData = new CorrelationData(String.valueOf(message.getId()));

        // 设置发送者回调函数
        correlationData.getFuture().addCallback(
                // 消息发送成功的回调
                result->{
                    // 如果消息已经被确认过了
                    if(result.isAck())
                    {
                        log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
                        //删除消息表中的记录
                        mqMessageService.completed(message.getId());
                    }
                    // 发送成功但是确认失败
                    else
                    {
                        // 3.2.nack，消息失败
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}",correlationData.getId(), result.getReason());
                    }
                },
                // 消息发送过程异常
                ex -> log.error("消息发送异常, ID:{}, 原因{}",correlationData.getId(),ex.getMessage())
        );
        // 指定交换机进行发送
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT,"",mqMsg, correlationData);
    }

    /**
     * @author CalmKin
     * @description 保存支付记录
     * @version 1.0
     * @date 2024/2/4 14:32
     */
    public PayRecord createPayRecord(Orders orders) {

        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        // 业务幂等性，防止重复插入支付记录
        if (orders.getStatus().equals("600002")) {
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


    /**
     * @author CalmKin
     * @description 保存订单
     * @version 1.0
     * @date 2024/2/4 14:32
     */
    @Transactional
    public Orders saveOrders(String userId, CreateOrderDto createOrderDto) {
        // 幂等性处理（订单表有out_business_id唯一主键约束）
        // 对于选课订单来说，用的是选课记录id
        String outBusinessId = createOrderDto.getOutBusinessId();

        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOutBusinessId, outBusinessId));

        // 订单不存在时，才进行插入
        if (order != null) return order;

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
        if (insert <= 0) {
            throw new BusinessException("保存订单失败");
        }


        // ======================= 插入订单明细表 =======================
        // 获取订单id
        Long id = order.getId();

        List<OrdersGoods> ordersGoods = JSON.parseArray(orderDetail, OrdersGoods.class);
        // 给每一个订单明细拷贝订单
        for (OrdersGoods ordersGood : ordersGoods) {
            BeanUtils.copyProperties(order, ordersGood);
            ordersGood.setOrderId(id); // 设置明细对应的订单id
            goodsMapper.insert(ordersGood); // 插入到明细表
        }
        return order;
    }


}
