package com.order.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Delivery {
	private String shipId;
	private String orderId;
	private String orderUserId;
	private String orderUserName;
	private String orderDate;
	private int orderQty;
	private int orderAmount;
	private String shipAddress;
	private String shippingState;
	private int accumulatePoint;
}
