package com.product.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RequestDeliveryDetailDTO
{
	private String productName;
	private int qty;
}
