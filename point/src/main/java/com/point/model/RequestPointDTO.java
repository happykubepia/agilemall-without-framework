package com.point.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestPointDTO {
  private String orderId;
  private String userId;
  private int usePoint;		//주문시 사용 포인트 
  private int addPoint;			//구매가에 따른 부여 포인트 
}

