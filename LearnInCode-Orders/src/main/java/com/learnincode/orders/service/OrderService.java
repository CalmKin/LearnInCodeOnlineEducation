package com.learnincode.orders.service;

import com.learnincode.orders.model.dto.CreateOrderDto;
import com.learnincode.orders.model.dto.PayRecordDto;
import com.learnincode.orders.model.po.PayRecord;

public interface OrderService {

     PayRecordDto createOrder(String userId,CreateOrderDto createOrderDto);

    PayRecord getPayRecordByPayno(String payNo);

    PayRecordDto payresult(String payNo);
}
