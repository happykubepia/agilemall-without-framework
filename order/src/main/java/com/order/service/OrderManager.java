package com.order.service;

import java.util.HashMap;

import org.springframework.stereotype.Component;

/**
 * 클라이언트 요청을 관리한다.
 * 메시지 브로커로 수신되는 경우 메시지의 트랜잭션 ID를 이용하여 요청을 찾는다.
 */
@Component
public class OrderManager
{
	private HashMap<String, ResultListener> requests = new HashMap<>();

	/**
	 * @param trxId
	 * @param listener
	 */
	public void addOrder(String trxId, ResultListener listener) {
		requests.put(trxId, listener);
	}

	/**
	 * @param trxId
	 * @return
	 */
	public ResultListener getOrder(String trxId) {
		return requests.get(trxId);
	}

	/**
	 * @param trxId
	 */
	public void removeOrder(String trxId) {
		System.err.println("@.@ REMOVE: " + trxId);
		requests.remove(trxId);
	}
}
