package com.order.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RequestOrderDetailDTO
{
	private String productName;
	private int qty;
}

