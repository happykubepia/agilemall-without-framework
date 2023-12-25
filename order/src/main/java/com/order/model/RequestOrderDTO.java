package com.order.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RequestOrderDTO
{
	private String userId;
	private int usePoint;
	private List<RequestOrderDetailDTO> products;
}

