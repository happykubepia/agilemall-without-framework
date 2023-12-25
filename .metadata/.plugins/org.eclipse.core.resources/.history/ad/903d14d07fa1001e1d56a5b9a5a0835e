package com.payment.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 메시지 브로커를 통해 요청 메시지를 전달한다.
 */
@Setter
@Getter
@ToString
public class ChannelRequest<T>
{
	/*
	 * 요청 트랜잭션 아이디
	 */
	private String trxId;

	/*
	 * 메지지의 유형
	 * TRX: 저장 요청
	 * RLB: 롤백 요청
	 */
	private String messageType;

	/*
	 * 전달 데이터
	 */
	private T payload;
}
