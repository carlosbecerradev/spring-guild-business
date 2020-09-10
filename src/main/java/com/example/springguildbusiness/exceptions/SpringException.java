package com.example.springguildbusiness.exceptions;

public class SpringException extends RuntimeException {

	public SpringException(String exMessage, Exception exception) {
		super(exMessage, exception);
	}
	
	public SpringException(String exMessage) {
		super(exMessage);
	}
}
