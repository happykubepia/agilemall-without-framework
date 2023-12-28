package com.order.service;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.order.config.IChannel;

@Component
public class QueueComponent
{
	@Autowired
	private RabbitMessagingTemplate template;

	@Bean
	public Queue paymentQueue() {
		return new Queue(IChannel.CH_ORDER_PAYMENT, false);
	}
	/**
	 * @param payload
	 */
	public void sendPaymentMessage(String payload) {
		template.convertAndSend(IChannel.CH_ORDER_PAYMENT, payload);
	}

	@Bean
	public Queue deliveryQueue() {
		return new Queue(IChannel.CH_ORDER_DELIVERY, false);
	}
	/**
	 * @param payload
	 */
	public void sendDeliveryMessage(String payload) {
		template.convertAndSend(IChannel.CH_ORDER_DELIVERY, payload);
	}

	@Bean
	public Queue pointQueue() {
		return new Queue(IChannel.CH_ORDER_POINT, false);
	}
	/**
	 * @param payload
	 */
	public void sendPointMessage(String payload) {
		template.convertAndSend(IChannel.CH_ORDER_POINT, payload);
	}
	
	@Bean
	public Queue inventoryQueue() {
		return new Queue(IChannel.CH_DELIVERY_PRODUCT, false);
	}
	/**
	 * @param payload
	 */
	public void sendInventoryMessage(String payload) {
		template.convertAndSend(IChannel.CH_DELIVERY_PRODUCT, payload);
	}
	
	@Bean
	public Queue reportQueue() {
		return new Queue(IChannel.CH_REPORT, false);
	}
	/**
	 * @param payload
	 */
	public void sendReportMessage(String payload) {
		template.convertAndSend(IChannel.CH_REPORT, payload);
	}
		
}
