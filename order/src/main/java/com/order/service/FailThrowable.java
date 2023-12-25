package com.order.service;

import com.google.gson.JsonElement;

public class FailThrowable extends Throwable {
	/*
	 * source는 ChannelResponse 클래스의 JSON 객체이다.
	 */
	private JsonElement source;

	public FailThrowable(JsonElement source) {
		this.source = source;
	}

	public JsonElement getSource() {
		return source;
	}

	public String getErrorString() {
		if ( source != null ) {
			return source.getAsJsonObject().get("errorString").getAsString(); 
		}
		else {
			return "";
		}
	}
}
