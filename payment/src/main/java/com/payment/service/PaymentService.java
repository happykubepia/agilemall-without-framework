package com.payment.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.payment.config.IChannel;
import com.payment.dao.PaymentDao;
import com.payment.model.ChannelRequest;
import com.payment.model.ChannelResponse;
import com.payment.model.Payment;
import com.payment.model.RequestPaymentDTO;
import com.payment.model.ResponsePaymentDTO;

@Service
public class PaymentService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private PaymentDao paymentDao;

	@Autowired
	private RabbitMessagingTemplate queueTemplate;	
	/**
	 * 결제를 수행한다.
	 * 
	 * @param gs
	 * @param req
	 */
	public void startTransaction(Gson gs, ChannelRequest<RequestPaymentDTO> req)
	{
		/*
		 * 결제 수행 및 결과 반환
		 */
		String payId = UUID.randomUUID().toString();

		try {
			RequestPaymentDTO payload = req.getPayload();

			/*
			 * 결제 정보 저장
			 */
			log.info("Save Approval info =>"+payId);
			Payment pay = new Payment();
			pay.setPaymentId(payId);
			pay.setOrderId(payload.getOrderId());
			pay.setPaymentDate(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
			pay.setCardNumber(payload.getCardNumber());
			pay.setPaymentAmount(payload.getAmount());
			pay.setUserId(payload.getUserId());

			paymentDao.insertPayment(pay);

			/*
			 * 결제 결과를 전송한다. 
			 */
			log.info("Send Approval result =>"+payId);
			ResponsePaymentDTO resPayload = new ResponsePaymentDTO();
			resPayload.setOrderId(payload.getOrderId());
			resPayload.setPaymentId(payId);

			ChannelResponse<ResponsePaymentDTO> res = new ChannelResponse<>();
			res.setTrxId(req.getTrxId());
			res.setMessageType("PAYMENT");
			res.setReturnCode(true);
			res.setPayload(resPayload);

			queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
			//throw new Exception("Error during payment!");
		} catch(Exception e) {
			ChannelResponse<ResponsePaymentDTO> res = new ChannelResponse<>();
			res.setTrxId(req.getTrxId());
			res.setMessageType("PAYMENT");
			res.setReturnCode(false);
			res.setErrorString(e.getMessage());
			queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
		}
	}

	/**
	 * 주문 ID를 이용하여 결제 정보를 삭제한다.
	 * 
	 * @param req
	 */
	public void rollback(ChannelRequest<RequestPaymentDTO> req)
	{
		log.info("Cancel payment record=>"+req.getTrxId());
		try {
			paymentDao.deletePayment(req.getPayload().getOrderId());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
