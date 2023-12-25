package com.delivery.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.delivery.model.Delivery;
import com.delivery.model.DeliveryDetail;

@Mapper
@Repository
public interface DeliveryDao {
  public int insertDelivery(Delivery delivery);
  public int insertDeliveryDetail(DeliveryDetail deliveryDetail);
  public int deleteDelivery(String orderId);
  public int deleteDeliveryDetail(String orderId);
  public Delivery selectDelivery(String orderId);
}
