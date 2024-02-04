package com.learnincode.orders.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.orders.config.AlipayConfig;
import com.learnincode.orders.model.dto.CreateOrderDto;
import com.learnincode.orders.model.dto.PayRecordDto;
import com.learnincode.orders.model.po.PayRecord;
import com.learnincode.orders.service.OrderService;
import com.learnincode.orders.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Slf4j
@Api(value = "订单支付接口", tags = "订单支付接口")
public class OrderController {

    // 请求支付所需参数
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;
    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @Autowired
    OrderService orderService;

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody CreateOrderDto createOrderDto) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = user.getId();

        return orderService.createOrder(userId,createOrderDto);
    }


    /**
     * @param payNo 支付流水号，前端从generatePayCode的返回值里面取出来
     * @param httpResponse 支付页面
     * @throws IOException
     */
    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {
        // 先查询支付号对应的支付记录
        PayRecord payRecord =  orderService.getPayRecordByPayno(payNo);

        //如果payNo不存在则提示重新发起支付
        if(payRecord == null)
        {
            throw new BusinessException("请重新点击支付获取二维码");
        }

        // 因为扫码之后，二维码会刷新
        // 为了防止重复支付，需要先判断支付的状态
        String status = payRecord.getStatus();
        if("601002".equals(status))
        {
            throw new BusinessException("订单已支付，请勿重复支付。");
        }

        // 通过支付宝SDK，发起支付请求
        AlipayClient client = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);//获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");

//        alipayRequest.setNotifyUrl("http://tjxt-user-t.itheima.net/xuecheng/orders/paynotify");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\""+payRecord.getPayNo()+"\"," + // 支付流水号
                " \"total_amount\":\""+payRecord.getTotalPrice()+"\"," +    // 总金额
                " \"subject\":\""+payRecord.getOrderName()+"\"," +  // 商品名称
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数

        // 获取支付页面
        String form = "";
        try {
            //请求支付宝下单接口,发起http请求
            form = client.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();


    }


    @ApiOperation("根据支付流水号，主动向支付宝查询支付状态")
    @GetMapping("/payresult")
    @ResponseBody
    public PayRecordDto payresult(String payNo) throws IOException {

        //查询支付结果
        return orderService.payresult(payNo);
    }


}
