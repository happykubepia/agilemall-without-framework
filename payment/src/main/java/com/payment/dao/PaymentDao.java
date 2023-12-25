package com.payment.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.payment.model.Payment;

@Mapper
@Repository
public interface PaymentDao {
	public void insertPayment(Payment payment);
	public void deletePayment(String orderId);
}

