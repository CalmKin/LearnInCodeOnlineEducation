package com.learnincode.orders.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.orders.config.AlipayConfig;
import com.learnincode.orders.model.dto.CreateOrderDto;
import com.learnincode.orders.model.dto.PayRecordDto;
import com.learnincode.orders.model.dto.PayStatusDto;
import com.learnincode.orders.model.po.PayRecord;
import com.learnincode.orders.service.OrderService;
import com.learnincode.orders.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

        return orderService.createOrder(userId, createOrderDto);
    }


    /**
     * @param payNo        支付流水号，前端从generatePayCode的返回值里面取出来
     * @param httpResponse 支付页面
     * @throws IOException
     */
    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {
        // 先查询支付号对应的支付记录
        PayRecord payRecord = orderService.getPayRecordByPayno(payNo);

        //如果payNo不存在则提示重新发起支付
        if (payRecord == null) {
            throw new BusinessException("请重新点击支付获取二维码");
        }

        // 因为扫码之后，二维码会刷新
        // 为了防止重复支付，需要先判断支付的状态
        String status = payRecord.getStatus();
        if ("601002".equals(status)) {
            throw new BusinessException("订单已支付，请勿重复支付。");
        }

        // 通过支付宝SDK，发起支付请求
        AlipayClient client = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);//获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request

        //在公共参数中设置回跳和通知地址
        alipayRequest.setNotifyUrl("http://服务ip:端口/orders/paynotify");
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"" + payRecord.getPayNo() + "\"," + // 支付流水号
                " \"total_amount\":\"" + payRecord.getTotalPrice() + "\"," +    // 总金额
                " \"subject\":\"" + payRecord.getOrderName() + "\"," +  // 商品名称
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
    public PayRecordDto payresult(String payNo) {
        //查询支付结果
        return orderService.queryPayResult(payNo);
    }

    /**
     * @author CalmKin
     * @description 接收支付回调地址, 请求的路径和上面设置的setNotifyUrl要保持一致
     * @version 1.0
     * @date 2024/2/4 10:52
     */
    @PostMapping("/paynotify")
    public void paynotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();

        //
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        // 对请求进行验签
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if (verify_result) {//验证成功
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");

            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");

            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");

            //总金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");


            //交易成功
            if (trade_status.equals("TRADE_SUCCESS")) {
                System.out.println(trade_status);

                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setOut_trade_no(out_trade_no);
                payStatusDto.setTrade_no(trade_no);
                payStatusDto.setTrade_status(trade_status);
                payStatusDto.setApp_id(APP_ID);
                payStatusDto.setTotal_amount(total_amount);

                orderService.saveAliPayStatus(payStatusDto);

            }
            response.getWriter().write("success");
        } else {
            response.getWriter().write("fail");
        }


    }


}
