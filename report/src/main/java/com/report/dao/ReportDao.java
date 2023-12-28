package com.report.dao;

import org.springframework.stereotype.Repository;

import com.report.model.*;

import org.apache.ibatis.annotations.Mapper;

@Mapper
@Repository
public interface ReportDao {
	Report selectReport(String orderId) throws Exception;

	int insertReport(Report report) throws Exception;

	int updateDelivery(Delivery4ReportDTO delivery) throws Exception;
	int updateOrder(Order4ReportDTO order) throws Exception;
	int updatePay(Pay4ReportDTO pay) throws Exception;
	int updatePoint(Point4ReportDTO point) throws Exception;
	
	int deleteReport(String orderId) throws Exception;
	
}
