package com.order.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.order.model.Order;
import com.order.model.OrderDetail;

@Mapper
@Repository
public interface OrderDao {
	Order selectOrder(String orderId);
	public void insertDetail(OrderDetail detail);
	public void insertOrder(Order order);
	public void deleteOrder(String orderId);
	public void deleteOrderDetail(String orderId);
}
