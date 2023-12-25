package com.order.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderState {
	private String orderId;
	private String userName;
	private String orderDT;
	private int orderAmount;
	private String shipAddress;
	private String deliveryState;
}
