package com.point.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.point.config.IChannel;
import com.point.model.ChannelRequest;
import com.point.model.ChannelResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.point.dao.PointRepository;
import com.point.model.Point;
import com.point.model.Point4ReportDTO;
import com.point.model.RequestPointDTO;
import com.point.model.ResponsePointDTO;
import com.point.model.ResultVO;
import com.point.model.UserPointDTO;

@Service
public class PointService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PointRepository pointRepository;

	@Autowired
	private RabbitMessagingTemplate queueTemplate;
	

	public ResponseEntity<ResultVO<Point>> register(Point point) {
		pointRepository.insert(point);
		
		return new ResponseEntity<ResultVO<Point>>(
				ResultVO.<Point>builder()
				.returnCode(true)
				.result(pointRepository.findByUserId(point.getUserId()))
				.build(), HttpStatus.OK);

	}
	
	public ResponseEntity<ResultVO<Point>> search(String userId) {
		log.info("search point => "+userId);
		
		return new ResponseEntity<ResultVO<Point>>(
				ResultVO.<Point>builder()
				.returnCode(true)
				.result(pointRepository.findByUserId(userId))
				.build(), HttpStatus.OK);

	}

	public ResponseEntity<ResultVO<Point>> minus(String userId, int point) {
		log.debug("point MINUS => "+userId+":"+point);
		
		ResultVO<Point> result = null;
		try {
			/*
			 * MongoDB 갱신
			 */
			changePoint(userId, -point);

			result = ResultVO.<Point>builder()
					.returnCode(true)
					.result(pointRepository.findByUserId(userId))
					.build();
		} catch (Exception e) {
			result = ResultVO.<Point>builder()
					.returnCode(false)
					.returnMessage(e.getMessage())
					.build();
		}

		return new ResponseEntity<ResultVO<Point>>(result, HttpStatus.OK);
	}
	

	public ResponseEntity<ResultVO<Point>> plus(String userId, int point) {
		log.debug("point PLUS => "+userId+":"+point);
		
		ResultVO<Point> result = null;
		try {
			/*
			 * MongoDB 갱신
			 */
			changePoint(userId, point);

			result = ResultVO.<Point>builder()
					.returnCode(true)
					.result(pointRepository.findByUserId(userId))
					.build();
		} catch (Exception e) {
			result = ResultVO.<Point>builder()
					.returnCode(false)
					.returnMessage(e.getMessage())
					.build();
		}

		return new ResponseEntity<ResultVO<Point>>(result, HttpStatus.OK);
	}		

	private Point changePoint(String userId, int point) {
		Point obj = pointRepository.findByUserId(userId);

		if ( obj != null ) {
			log.info("current point:"+obj.getUserPoint()+", change point:"+point);
			obj.setUserPoint(obj.getUserPoint() + point);
			pointRepository.save(obj);
		}
		return obj;
	}
	
	/**
	 * 포인트 처리를 수행한다.
	 * 
	 * @param gs
	 * @param req
	 */
	public void startTransaction(Gson gs, ChannelRequest<RequestPointDTO> req)
	{
		/*
		 * 포인트 차감 처리 및 결과 반환
		 */

		try {
			
			RequestPointDTO payload = req.getPayload();
			Point obj = null;
			
			String userId = payload.getUserId();
			int usePoint = payload.getUsePoint();		//차감할 포인트
			int addPoint = payload.getAddPoint();		//구매가에 따른 부여할 포인트 
			
			obj = pointRepository.findByUserId(userId);
			if(obj == null) {
				ChannelResponse<ResponsePointDTO> res = new ChannelResponse<>();
				res.setTrxId(req.getTrxId());
				res.setMessageType("POINT");
				res.setReturnCode(false);
				res.setErrorString("User ID <"+userId+">에 대한 정보를 찾을 수 없음");
				queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
				return;
			}
			
			if(usePoint > 0) {
				if(obj.getUserPoint() < usePoint) {
					ChannelResponse<ResponsePointDTO> res = new ChannelResponse<>();
					res.setTrxId(req.getTrxId());
					res.setMessageType("POINT");
					res.setReturnCode(false);
					res.setErrorString("User ID <"+userId+">의 잔여 포인트가 부족함");
					queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
					return;
				} else {
					obj = changePoint(userId, -1 * usePoint);
				}
			}
			
			obj = changePoint(userId, addPoint);
			
			/*
			 * CQRS: Report 서비스에 주문정보 업데이트 메시지 발생 
			 */
			Point4ReportDTO rpt = new Point4ReportDTO();
			rpt.setOrderId(payload.getOrderId());
			rpt.setUserPoint(obj.getUserPoint());
			
			ChannelRequest<Point4ReportDTO> reqRpt = new ChannelRequest<>();
			reqRpt.setTrxId(req.getTrxId());
			reqRpt.setMessageType("point");
			reqRpt.setPayload(rpt);
			queueTemplate.convertAndSend(IChannel.CH_REPORT, gs.toJson(reqRpt));
			
			/*
			 * 처리 결과를 메시지 브로커로 전송한다. 
			 */
			ResponsePointDTO resPayload = new ResponsePointDTO();
			resPayload.setUserId(payload.getUserId());
			resPayload.setUserPoint(obj.getUserPoint());

			ChannelResponse<ResponsePointDTO> res = new ChannelResponse<>();
			res.setTrxId(req.getTrxId());
			res.setMessageType("POINT");
			res.setReturnCode(true);
			res.setPayload(resPayload);

			queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
		}catch(Exception e) {
			ChannelResponse<ResponsePointDTO> res = new ChannelResponse<>();
			res.setTrxId(req.getTrxId());
			res.setMessageType("POINT");
			res.setReturnCode(false);
			res.setErrorString(e.getMessage());
			queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
		}
	}

	/**
	 * 주문 ID를 이용하여 포인트 차감을 취소한다.
	 * 
	 * @param req
	 */
	public void rollback(ChannelRequest<RequestPointDTO> req)
	{
		RequestPointDTO payload = req.getPayload();
		
		int usePoint = payload.getUsePoint();		//차감했던 포인트
		int addPoint = payload.getAddPoint();		//구매가에 따른 부여했던 포인트 
		
		try {
			if(usePoint > 0) changePoint(payload.getUserId(), usePoint);
			changePoint(payload.getUserId(), -1 * addPoint);
			
			//주문 Report 서비스에 rollback 요청함 
			Gson gs = new GsonBuilder().setPrettyPrinting().create();
			ChannelRequest<Point4ReportDTO> reqRpt = new ChannelRequest<>();
			Point4ReportDTO rpt = new Point4ReportDTO();
			rpt.setOrderId(req.getPayload().getOrderId());
			reqRpt.setTrxId(req.getTrxId());
			reqRpt.setMessageType("RBL");
			reqRpt.setPayload(rpt);
			queueTemplate.convertAndSend(IChannel.CH_REPORT, gs.toJson(reqRpt));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}

