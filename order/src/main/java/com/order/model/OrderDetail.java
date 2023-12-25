package com.order.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderDetail
{
	private String orderId;
	private String productName;  
	private int price;
	private int orderQty;
}

