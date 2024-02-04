package com.learnincode.orders.model.dto;

import com.learnincode.orders.model.po.PayRecord;
import lombok.Data;
import lombok.ToString;

/**
 * @description 支付记录dto
 */
@Data
@ToString
public class PayRecordDto extends PayRecord {

    //二维码
    private String qrcode;

}
