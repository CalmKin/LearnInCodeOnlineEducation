package com.learnincode.orders.service;

import com.learnincode.orders.model.dto.CreateOrderDto;
import com.learnincode.orders.model.dto.PayRecordDto;

public interface OrderService {
    PayRecordDto generatePayCode(String userId, CreateOrderDto createOrderDto);

     PayRecordDto createOrder(String userId,CreateOrderDto createOrderDto);

}
