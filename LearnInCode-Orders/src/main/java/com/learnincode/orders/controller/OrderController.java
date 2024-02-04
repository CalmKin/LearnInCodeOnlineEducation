package com.learnincode.orders.controller;

import com.learnincode.orders.model.dto.CreateOrderDto;
import com.learnincode.orders.model.dto.PayRecordDto;
import com.learnincode.orders.service.OrderService;
import com.learnincode.orders.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Slf4j
@Api(value = "订单支付接口", tags = "订单支付接口")
public class OrderController {

    @Autowired
    OrderService orderService;

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody CreateOrderDto createOrderDto) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = user.getId();

        return orderService.generatePayCode(userId,createOrderDto);
    }


    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {

    }


}
