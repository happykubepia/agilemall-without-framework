package com.order.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductInventoryDTO {
	private String orderId;
	private List<RequestOrderDetailDTO> products;
}
