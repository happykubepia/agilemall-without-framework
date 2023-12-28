package com.payment.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pay4ReportDTO {
	private String orderId;
	private String payDtm;
	private int payAmt;
	private String cardNumber;
}
