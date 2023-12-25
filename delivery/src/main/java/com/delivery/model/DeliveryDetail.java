package com.delivery.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class DeliveryDetail
{
	private String shipId;
	private String orderId;
	private String orderProdNm;
	private int orderQty;
}
