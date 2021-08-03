package com.rest.publicpoll;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * Copyright © 2021 Alexander Aghili - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexander Aghili alexander.w.aghili@gmail.com, May 2021
 */

public class PollAnswer {

	private String pollID;
	private String answer;
	private String letter;
	private ArrayList<String> userIDs;
	
	//Constructor designed for init of PollAnswer when 
	public PollAnswer(String letter, String answer) {
		this.letter = letter;
		this.answer = answer;
	}
	
	public PollAnswer(String letter, String answer, ArrayList<String> userIDs) {
		this.answer = answer;
		this.letter = letter;
		this.userIDs = userIDs;
	}
	
	public PollAnswer(String pollID, String letter, String answer, ArrayList<String> userIDs) {
		this.pollID = pollID;
		this.answer = answer;
		this.letter = letter;
		this.userIDs = userIDs;
	}
	
	public String getID() {
		return pollID;
	}
	
	public void setID(String pollID) {
		this.pollID = pollID;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getLetter() {
		return letter;
	}

	public void setLetter(String letter) {
		this.letter = letter;
	}

	
	public ArrayList<String> getUserIDs() {
		return userIDs;
	}

	public void setUserIDs(ArrayList<String> userIDs) {
		this.userIDs = userIDs;
	}

	@Override
	public String toString() {
		return letter+") " + answer;
	}
	
	public String toJSON() {
		String json =  "{\"pollID\": \"" + pollID + "\", \"letter\": \"" + letter + "\", \"answer\": \"" + answer + "\", \"users\": [";
		for (int i = 0; i < userIDs.size(); i++) {
			json += "\"" + userIDs.get(i) + "\",";
		}
		return json.substring(0, json.length() - 1) + "]}";
		
	}
	
	public static PollAnswer fromJSON(String pollAnswerJSON) {
		JSONObject pollAnswerJO = new JSONObject(pollAnswerJSON);
		String pollID = pollAnswerJO.getString("pollID");
		String letter = pollAnswerJO.getString("letter");
		String answer = pollAnswerJO.getString("answer");
		JSONArray ja = pollAnswerJO.getJSONArray("users");
		
		ArrayList<String> userIDs = new ArrayList<String>();
		for (int i = 0; i < ja.length(); i++) {
			userIDs.add(ja.getString(i));
		}
		return new PollAnswer(pollID, letter, answer, userIDs);
	}
	
}
