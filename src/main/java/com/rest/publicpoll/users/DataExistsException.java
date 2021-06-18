package com.rest.publicpoll.users;

public class DataExistsException extends Exception {
	
	public DataExistsException(String param) {
		super("The " + param + " exists");
	}

}
