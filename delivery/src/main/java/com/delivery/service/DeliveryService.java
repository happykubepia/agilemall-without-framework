package com.delivery.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.delivery.config.IChannel;
import com.delivery.config.Constant;
import com.delivery.dao.DeliveryDao;
import com.delivery.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.delivery.model.RequestDeliveryDetailDTO;

@Service
public class DeliveryService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private DeliveryDao deliveryDao;

	@Autowired
	private RabbitMessagingTemplate queueTemplate;

	//배송상태 조회 
	public ResponseEntity<ResultVO<Delivery>> get(String orderId) {
		ResultVO<Delivery> result = new ResultVO<>();
		Delivery ship = deliveryDao.selectDelivery(orderId);
		if (ship != null) {
			result.setReturnCode(true);
			result.setResult(ship);
		} else {
			result.setReturnCode(false);
			result.setReturnMessage("지정된 주문 정보가 없습니다.");
		}

		return new ResponseEntity<ResultVO<Delivery>>(result, HttpStatus.OK);

	}
	//배송정보 등록
	public ResponseEntity<String> setDeliveryInsert(Delivery delivery) {
		return new ResponseEntity<String> (deliveryDao.insertDelivery(delivery)+"", HttpStatus.OK);

	}

	/**
	 * 배달처리를 수행한다.
	 * 
	 * @param gs
	 * @param req
	 */
	public void startTransaction(Gson gs, ChannelRequest<RequestDeliveryDTO> req)
	{
		/*
		 * 결제 수행 및 결과 반환
		 */
		String shipId = UUID.randomUUID().toString();

		try {
			RequestDeliveryDTO payload = req.getPayload();
			
			//배달 리스트 저장 
			DeliveryDetail detail = null;
			for(RequestDeliveryDetailDTO item : payload.getProducts()) {
				detail = new DeliveryDetail();
				detail.setShipId(shipId);
				detail.setOrderId(payload.getOrderId());
				detail.setOrderProdNm(item.getProductName());
				detail.setOrderQty(item.getQty());
				
				deliveryDao.insertDeliveryDetail(detail);
			}

			/*
			 * 배달 정보 저장
			 */
			Delivery delivery = new Delivery();
			delivery.setShipId(shipId);
			delivery.setOrderId(payload.getOrderId());
			delivery.setOrderUserId(payload.getOrderUserId());
			delivery.setOrderUserName(payload.getOrderUserName());
			delivery.setOrderDate(payload.getOrderDate());
			delivery.setShipAddress(payload.getShipAddress());
			delivery.setShippingState(Constant.DELIVERY_STATE_ING);

			deliveryDao.insertDelivery(delivery);

			/*
			 * CQRS: Report 서비스에 주문정보 업데이트 메시지 발생 
			 */
			Delivery4ReportDTO rpt = new Delivery4ReportDTO();
			rpt.setOrderId(payload.getOrderId());
			rpt.setShipAddr(payload.getShipAddress());
			rpt.setShipState(Constant.DELIVERY_STATE_ING);
			ChannelRequest<Delivery4ReportDTO> reqRpt = new ChannelRequest<>();
			reqRpt.setTrxId(req.getTrxId());
			reqRpt.setMessageType("delivery");
			reqRpt.setPayload(rpt);
			queueTemplate.convertAndSend(IChannel.CH_REPORT, gs.toJson(reqRpt));
			
			/*
			 * 결과를 메시지 브로커로 전송한다. 
			 */
			ResponseDeliveryDTO resPayload = new ResponseDeliveryDTO();
			resPayload.setShipId(shipId);
			resPayload.setOrderId(payload.getOrderId());

			ChannelResponse<ResponseDeliveryDTO> res = new ChannelResponse<>();
			res.setTrxId(req.getTrxId());
			res.setMessageType("DELIVERY");
			res.setReturnCode(true);
			res.setPayload(resPayload);

			queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
			//throw new Exception("Error during insert delivery detail!");
		}catch(Exception e) {
			ChannelResponse<ResponseDeliveryDTO> res = new ChannelResponse<>();
			res.setTrxId(req.getTrxId());
			res.setMessageType("DELIVERY");
			res.setReturnCode(false);
			res.setErrorString(e.getMessage());
			queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
		}
	}

	/**
	 * 주문 ID를 이용하여 배달 정보를 삭제한다.
	 * 
	 * @param req
	 */
	public void rollback(ChannelRequest<RequestDeliveryDTO> req)
	{
		try {
			deliveryDao.deleteDelivery(req.getPayload().getOrderId());
			deliveryDao.deleteDeliveryDetail(req.getPayload().getOrderId());
			
			//주문 Report 서비스에 rollback 요청함 
			Gson gs = new GsonBuilder().setPrettyPrinting().create();
			ChannelRequest<Delivery4ReportDTO> reqRpt = new ChannelRequest<>();
			Delivery4ReportDTO rpt = new Delivery4ReportDTO();
			rpt.setOrderId(req.getPayload().getOrderId());
			reqRpt.setTrxId(req.getTrxId());
			reqRpt.setMessageType("RBL");
			reqRpt.setPayload(rpt);
			queueTemplate.convertAndSend(IChannel.CH_REPORT, gs.toJson(reqRpt));
			
			log.info("##### Rollback processed !!");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
