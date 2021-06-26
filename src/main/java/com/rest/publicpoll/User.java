package com.rest.publicpoll;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rest.publicpoll.users.PasswordEncryption;

/*
 * Copyright © 2021 Alexander Aghili - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexander Aghili alexander.w.aghili@gmail.com, June 2021
 */

 
public class User 
{
	private String userID;
	private String username;
	private String email;
	private BirthDay birthday;
	private String firstname;
	private String lastname;
	private String gender;
	private String password;
	private String profilePicture;
	//Store polls by ID to be loaded in when at page instead of at request time
	private ArrayList<String> savedPollsID;
	private ArrayList<String> recentlyRespondedToPollsID;
	private ArrayList<String> myPollsID;
	
	//All info minus password because that ins't needed
	public User(String userID, String username, String email, BirthDay birthday, 
			String firstname, String lastname, String gender, String profilePicture, ArrayList<String> savedPollsID, ArrayList<String> recentlyRespondedToPollsID, ArrayList<String> myPollsID) {
		this.userID = userID;
		this.username = username;
		this.email = email;
		this.birthday = birthday;
		this.firstname = firstname;
		this.lastname = lastname;
		this.gender = gender;
		this.profilePicture = profilePicture;
		this.savedPollsID = savedPollsID;
		this.recentlyRespondedToPollsID = recentlyRespondedToPollsID;
		this.myPollsID = myPollsID;
	}
	
	//Creation of User
	public User(String username, String email, BirthDay birthday, String firstname, String lastname, String gender, String password, String profilePicture) {
		this.username = username;
		this.email = email;
		this.birthday = birthday;
		this.firstname = firstname;
		this.lastname = lastname;
		this.password = password;
		this.gender = gender;
		this.profilePicture = profilePicture;
		savedPollsID = new ArrayList<String>();
		recentlyRespondedToPollsID = new ArrayList<String>();
		myPollsID = new ArrayList<String>(); 
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
		//Technically encoding but who cares.
		try {
			return PasswordEncryption.generateStrongPasswordHash(password);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return "";
		}
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}
	
	public ArrayList<String> getMyPollsID() {
		return myPollsID;
	}

	public void setMyPollsID(ArrayList<String> myPollsID) {
		this.myPollsID = myPollsID;
	}	
	
	//Birthday doesn't work
	public static User newUserFromJSON(String jsonString) { 
		JSONObject jo = new JSONObject(jsonString);
		String username = jo.getString("username");
		String email = jo.getString("email");
		BirthDay birthday = getBirthday(jo.getJSONObject("birthday"));
		String firstname = jo.getString("firstname");
		String lastname = jo.getString("lastname");
		String gender = jo.getString("gender");
		String passwordPlain = jo.getString("password");
		String profilePicture = jo.getString("profilePicture");
		
		return new User(username, email, birthday, firstname, lastname, gender, passwordPlain, profilePicture);
	}
	private static BirthDay getBirthday(JSONObject jo) {
		int year = jo.getInt("year");
		int month = jo.getInt("month");
		int day = jo.getInt("day");
		return new BirthDay(year, month, day);
	}
	
	public String toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("uid", userID);
		jo.put("username", username);
		jo.put("email", email);
		jo.put("birthday", new JSONObject(birthday.toJSON()));
		jo.put("firstname", firstname);
		jo.put("lastname", lastname);
		jo.put("gender", gender);
		jo.put("profilePicture", profilePicture);
		jo.put("savedPolls", getJSONArrayFromStringArrayList(savedPollsID));
		jo.put("recentPolls", getJSONArrayFromStringArrayList(recentlyRespondedToPollsID));
		jo.put("myPolls", getJSONArrayFromStringArrayList(myPollsID));
		return jo.toString();
	}
	
	private JSONArray getJSONArrayFromStringArrayList(ArrayList<String> pollIDs) {
		JSONArray ja = new JSONArray();
		for (int i = 0; i < pollIDs.size(); i++) {
			ja.put(pollIDs.get(i));
		}
		return ja;
	}

}
