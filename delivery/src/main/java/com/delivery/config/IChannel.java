package com.delivery.config;

public interface IChannel
{
	/*
	 * 요청 메시지 토픽
	 */
	public static final String CH_ORDER_PAYMENT = "CH-ORDER-PAYMENT_HIONDAL";
	public static final String CH_ORDER_DELIVERY = "CH-ORDER-DELIVERY_HIONDAL";
	public static final String CH_ORDER_POINT = "CH-ORDER-POINT_HIONDAL";
	/*
	 * 포인트 서비스에서 고객 서비스로 메시지 전달
	 */
	public static final String CH_POINT_CUSTOMER = "CH-POINT-CUSTOMER_HIONDAL";
	
	/*
	 * 배송 서비스에서 제품 서비스로 메시지 전달
	 */
	public static final String CH_DELIVERY_PRODUCT = "CH-DELIVERY-PRODUCT_HIONDAL";
	
	/*
	 * 응답 메시지 토픽
	 */
	public static final String CH_ORDER_RESPONSE = "CH-ORDER-RESPONSE_HIONDAL";
}
