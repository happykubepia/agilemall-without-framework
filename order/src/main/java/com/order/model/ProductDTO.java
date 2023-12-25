package com.order.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductDTO {
	private String id;
	private String productName;
	private long price;
	private int inventoryQty;
}
