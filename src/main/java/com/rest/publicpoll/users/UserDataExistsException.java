package com.rest.publicpoll.users;

public class UserDataExistsException extends Exception {
	
	private String param;
	
	public UserDataExistsException(String param) {
		super("The " + param + " exists");
		this.param = param;
	}
	
	public String getIssueParameter() {
		return param;
	}
	
	

}
