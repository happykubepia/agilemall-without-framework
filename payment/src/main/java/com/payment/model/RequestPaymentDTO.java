package com.payment.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestPaymentDTO {
	private String orderId;
	private String userId;
	private int qty;
	private int amount;
	private String cardNumber;
}
