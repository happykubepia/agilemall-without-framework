package com.payment.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment {
	private String paymentId;
	private String paymentDate;
	private int paymentAmount;
	private String cardNumber;
	private String userId;
	private String orderId;
}
