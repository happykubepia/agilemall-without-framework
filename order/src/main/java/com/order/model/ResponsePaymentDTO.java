package com.order.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponsePaymentDTO {
  private String paymentId;
  private String orderId;
}
