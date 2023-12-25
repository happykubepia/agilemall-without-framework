package com.order.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
public class User {
	private String userId;
	private String userName;
	private String address;
	private String cardNumber;
	private long pointNumber;
}

