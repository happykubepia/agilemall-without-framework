package com.order.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultVO<T> {
	private boolean returnCode;
	private String returnMessage;
	private T result;
}
