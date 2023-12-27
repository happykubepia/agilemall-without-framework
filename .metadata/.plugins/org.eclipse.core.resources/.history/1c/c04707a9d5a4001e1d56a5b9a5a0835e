package com.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.order.model.OrderEvent;

import reactor.core.publisher.FluxSink;

/**
 * 메시지 브로커로 전달된 메시지를 Flux 스트림에 제공한다.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ResultListener
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	private FluxSink<JsonElement> sink;

	public void register(FluxSink sink)
	{
		this.sink = sink;
	}

	/**
	 * @param data MQ 수신 데이터
	 */
	public void fireData(JsonElement recvData)
	{
		//OrderEvent event = sink.currentContext().get("orderEvent");
		OrderEvent event = sink.contextView().get("orderEvent");
		JsonObject jsonObj = ((JsonElement)recvData).getAsJsonObject();
		String type = jsonObj.get("messageType").getAsString();
		
		if ( event == null ) {
			log.info("#### ["+type+"] Can't get OrderEvent object");
		} else {
			log.info("#### ["+type+"] Order ID:" + event.getOrderId());
		}

		if ( "PAYMENT".equals(type) ) {
			event.setPaymentCompleted(true);
		}
		else if ( "DELIVERY".equals(type) ) {
			event.setDeliveryCompleted(true);
		}
		else if ( "POINT".equals(type) ) {
			event.setPointCompleted(true);
		}
		else if ( "PRODUCT".equals(type) ) {
			event.setProductCompleted(false);
		}

		boolean returnCode = jsonObj.get("returnCode").getAsBoolean();

		if ( returnCode == false ) {
			/*
			 * 참여자의 작업이 실패한 경우 완료 처리한다.
			 */
			//sink.complete();
			sink.error(new FailThrowable(recvData));
		}
		else {
			// 메시지 전달
			sink.next(recvData);

			if ( event.isDeliveryCompleted() && event.isPaymentCompleted() && event.isPointCompleted() && event.isProductCompleted()) {
				// 결제, 배송, 포인트, 제품 서비스 수행 결과를 수신하면 완료
				sink.complete();
			}
		}

	}
}
