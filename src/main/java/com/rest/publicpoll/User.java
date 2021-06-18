package com.rest.publicpoll;

import java.util.ArrayList;

/*
 * Copyright © 2021 Alexander Aghili - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexander Aghili alexander.w.aghili@gmail.com, June 2021
 */


/*
 * TODO:
 * Add My Polls
 * Encrypt Password
 */

public class User 
{
	private String userID;
	private String username;
	private String email;
	private BirthDay birthday;
	private String firstname;
	private String lastname;
	@SuppressWarnings("unused")
	private String password;
	//Store polls by ID to be loaded in when at page instead of at request time
	private ArrayList<String> savedPollsID;
	private ArrayList<String> recentlyRespondedToPollsID;
	
	//All info
	public User(String userID, String username, String email, BirthDay birthday, 
			String firstname, String lastname, ArrayList<String> savedPollsID, ArrayList<String> recentlyRespondedToPollsID) {
		this.userID = userID;
		this.username = username;
		this.email = email;
		this.birthday = birthday;
		this.firstname = firstname;
		this.lastname = lastname;
		this.savedPollsID = savedPollsID;
		this.recentlyRespondedToPollsID = recentlyRespondedToPollsID;
	}
	
	//Creation of User
	public User(String username, String email, BirthDay birthday, String firstname, String lastname, String password) {
		this.username = username;
		this.email = email;
		this.birthday = birthday;
		this.firstname = firstname;
		this.lastname = lastname;
		this.password = password;
		savedPollsID = new ArrayList<String>();
		recentlyRespondedToPollsID = new ArrayList<String>();
	}

	public String getUserID() {
		return userID;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BirthDay getBirthday() {
		return birthday;
	}

	public void setBirthday(BirthDay birthday) {
		this.birthday = birthday;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public ArrayList<String> getSavedPollsID() {
		return savedPollsID;
	}

	public void setSavedPollsID(ArrayList<String> savedPollsID) {
		this.savedPollsID = savedPollsID;
	}

	public void addToSavedPollsID(String pollID) {
		savedPollsID.add(pollID);
	}

	public ArrayList<String> getRecentlyRespondedToPollsID() {
		return recentlyRespondedToPollsID;
	}

	public void setRecentlyRespondedToPollsID(ArrayList<String> recentlyRespondedToPollsID) {
		this.recentlyRespondedToPollsID = recentlyRespondedToPollsID;
	}
	
	public void addToRecentlyRespondedPollsID(String pollID) {
		recentlyRespondedToPollsID.add(pollID);
	}
	
	public String getEncryptedPassword() {
		return this.password;
	}
	
}
