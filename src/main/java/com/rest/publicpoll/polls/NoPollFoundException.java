package com.rest.publicpoll.polls;

public class NoPollFoundException extends Exception{

	public NoPollFoundException(String pollID) {
		super("No Poll Could Be Found by the ID: " + pollID);
	}
	
}
