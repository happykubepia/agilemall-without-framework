package com.report.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order4ReportDTO {
	private String orderId;
	private String orderUserId;
	private String orderDtm;
	private int orderTotalAmount;
	private int accPnt;
	private int usePoint;
	private String orderDetail;
}
