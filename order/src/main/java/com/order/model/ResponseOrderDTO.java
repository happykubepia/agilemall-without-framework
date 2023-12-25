package com.order.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ResponseOrderDTO
{
	private String orderId;
	private String orderDate;
}
