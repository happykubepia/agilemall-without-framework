package com.delivery.rest;

import java.lang.reflect.Type;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delivery.config.IChannel;
import com.delivery.model.*;
import com.delivery.service.DeliveryService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name="Delivery service API", description="Delivery service API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DeliveryController {
	@Autowired
	private DeliveryService deliveryService;

	@GetMapping("/shippings/{orderId}")
	@Operation(summary="배송상태 조회")
	@Parameters({
		@Parameter(name="orderId", in=ParameterIn.PATH, description="", required=true, allowEmptyValue=false) 
	})
	public ResponseEntity<ResultVO<Delivery>> get(@PathVariable (name="orderId", required = true) String orderId) {
		return deliveryService.get(orderId);
	}
	@PostMapping("/shippings")
	@Operation(summary="배달 정보 등록하기 ")
	public ResponseEntity <String > setDeliveryInsert(
			@RequestBody Delivery delivery
			) throws Exception { 

		return deliveryService.setDeliveryInsert(delivery);
	}
	/*
	 * 응답 큐
	 */
	@Bean
	Queue responseQueue() {
		return new Queue(IChannel.CH_ORDER_RESPONSE, false);
	}

	/*
	 * 배송요청 큐
	 */
	@Bean
	Queue queue() {
		return new Queue(IChannel.CH_ORDER_DELIVERY, false);
	}

	@RabbitListener(queues = IChannel.CH_ORDER_DELIVERY)
	public void processMessage(String payload) {
		/*
		 * json 문자열을 객체로 변환
		 */
		System.out.println("[@.@ DELIVERY RECEIVED] " + payload.toString());
		
		Gson gs = new Gson();
		Type objType = new TypeToken<ChannelRequest<RequestDeliveryDTO>>(){}.getType();
		ChannelRequest<RequestDeliveryDTO> req = gs.fromJson(payload, objType);

		if ( "RLB".equals(req.getMessageType()) ) {
			deliveryService.rollback(req);
		}
		else {
			// TRX
			deliveryService.startTransaction(gs, req);
		}
	}

}
