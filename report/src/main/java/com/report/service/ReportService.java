package com.report.service;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.report.dao.ReportDao;
import com.report.model.*;

@Service
public class ReportService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private ReportDao reportDao;

	public ResponseEntity<Report> get(String orderId) {
		Report re = null;
		try {
			re = reportDao.selectReport(orderId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Report> (re, HttpStatus.OK);
	}

	public void startTransaction(String payload) {
		Gson gson = new Gson();
		JsonObject jsonObj = gson.fromJson(payload, JsonElement.class).getAsJsonObject();
		String type = jsonObj.get("messageType").getAsString();
		Type objType = null;
		log.info("#####["+type+"] ReportService > startTransaction: ");

		if("delivery".equalsIgnoreCase(type)) {
			objType = new TypeToken<ChannelRequest<Delivery4ReportDTO>>(){}.getType();
			ChannelRequest<Delivery4ReportDTO> req = gson.fromJson(payload, objType);
			Delivery4ReportDTO delivery = req.getPayload();
			updateDelivery(delivery);

		} else if("order".equalsIgnoreCase(type)) {
			objType = new TypeToken<ChannelRequest<Order4ReportDTO>>(){}.getType();
			ChannelRequest<Order4ReportDTO> req = gson.fromJson(payload, objType);
			Order4ReportDTO order = req.getPayload();
			updateOrder(order);
		} else if("pay".equalsIgnoreCase(type)) {
			objType = new TypeToken<ChannelRequest<Pay4ReportDTO>>(){}.getType();
			ChannelRequest<Pay4ReportDTO> req = gson.fromJson(payload, objType);
			Pay4ReportDTO pay = req.getPayload();
			updatePay(pay);
		} else if("point".equalsIgnoreCase(type)) {
			objType = new TypeToken<ChannelRequest<Point4ReportDTO>>(){}.getType();
			ChannelRequest<Point4ReportDTO> req = gson.fromJson(payload, objType);
			Point4ReportDTO point = req.getPayload();
			updatePoint(point);
		}
	}

	public void rollback(String payload) {
		Gson gson = new Gson();
		JsonObject jsonObj = gson.fromJson(payload, JsonElement.class).getAsJsonObject();
		String orderId = jsonObj.getAsJsonObject("payload").get("orderId").getAsString();
		log.info("##### ReportService > rollback: Order ID => "+orderId);
		
		try {
			Report report = reportDao.selectReport(orderId);
			if(report != null) reportDao.deleteReport(orderId);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void updateDelivery(Delivery4ReportDTO delivery) {
		log.info("Start updateDelivery");

		String orderId = delivery.getOrderId();
		if(!checkReport(orderId)) return;

		try {
			reportDao.updateDelivery(delivery);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void updateOrder(Order4ReportDTO order) {
		log.info("Start updateOrder");

		String orderId = order.getOrderId();
		if(!checkReport(orderId)) return;
		try {
			reportDao.updateOrder(order);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void updatePay(Pay4ReportDTO pay) {
		log.info("Start updatePay");

		String orderId = pay.getOrderId();
		if(!checkReport(orderId)) return;
		try {
			reportDao.updatePay(pay);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void updatePoint(Point4ReportDTO point) {
		log.info("Start updatePoint");

		String orderId = point.getOrderId();
		if(!checkReport(orderId)) return;
		try {
			reportDao.updatePoint(point);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkReport(String orderId) {
		Report report = null;
		try {
			report = reportDao.selectReport(orderId);
			if(report == null) {
				report = new Report();
				report.setOrderId(orderId);
				reportDao.insertReport(report);
			}
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
