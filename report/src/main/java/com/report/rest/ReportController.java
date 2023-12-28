package com.report.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.report.config.IChannel;
import com.report.model.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.report.service.ReportService;

@Tag(name="Report service API", description="Report service API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ReportService reportService;
	
	@GetMapping("/reports/{orderId}")
	@Operation(summary="주문 레포트 가져오기 ")
	@Parameters({
		@Parameter(name="orderId", in=ParameterIn.PATH, description="", required=true, allowEmptyValue=false) 
	})
	public ResponseEntity<Report> get(@PathVariable (name="orderId", required = true) String orderId) {
		return reportService.get(orderId);
	}
	
	/*
	 * 메시지 큐
	 */
	@Bean
	Queue queue() {
		return new Queue(IChannel.CH_REPORT, false);
	}

	@RabbitListener(queues = IChannel.CH_REPORT)
	public void processMessage(String payload) { 
		log.info("[@.@ REPORT RECEIVED] " + payload.toString());
		Gson gson = new Gson();
		JsonObject jsonObj = gson.fromJson(payload, JsonElement.class).getAsJsonObject();
		String type = jsonObj.get("messageType").getAsString();
		try {
			if("RBL".equalsIgnoreCase(type)) {
				reportService.rollback(payload);
			} else {
				reportService.startTransaction(payload);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
