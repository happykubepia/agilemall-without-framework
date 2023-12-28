package com.report.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Report {
	private String orderId;
	private String orderUsrId;
	private String orderDtm;
	private int orderTotAmt;
	private int accPnt;
	private int usePoint;
	private String orderDetail;
	private String payDtm;
	private int payAmt;
	private String cardNumber; 
	private String shipAddr;
	private String shipState;
	private int userPoint;
}
