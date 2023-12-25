package com.order.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 메시지 브로커를 통해 응답 메시지를 전달한다.
 */
@Setter
@Getter
@ToString
public class ChannelResponse<T>
{
	/*
	 * 요청 트랜잭션 아이디
	 */
	private String trxId;

	/*
	 * 처리 결과
	 * true - 정상수행
	 * false - 오류
	 */
	private boolean returnCode;

	/*
	 * 오류 메시지
	 */
	private String errorString;

	/*
	 * 메시지 유형
	 */
	private String messageType;

	/*
	 * 전달 데이터
	 */
	private T payload;
}

