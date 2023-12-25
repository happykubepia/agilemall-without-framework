package com.order.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ResponseDeliveryDTO
{
  private String orderId;
  private String shipId;
}
