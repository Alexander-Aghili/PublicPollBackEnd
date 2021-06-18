package com.rest.publicpoll;

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
	private int numClicked;
	
	//Constructor designed for init of PollAnswer when 
	public PollAnswer(String letter, String answer) {
		this.letter = letter;
		this.answer = answer;
		this.numClicked = 0;
	}
	
	public PollAnswer(String letter, String answer, int numClicked) {
		this.answer = answer;
		this.letter = letter;
		this.numClicked = numClicked;
	}
	
	public PollAnswer(String pollID, String letter, String answer, int numClicked) {
		this.pollID = pollID;
		this.answer = answer;
		this.letter = letter;
		this.numClicked = numClicked;
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

	public int getNumClicked() {
		return numClicked;
	}

	public void setNumClicked(int numClicked) {
		this.numClicked = numClicked;
	}
	
	@Override
	public String toString() {
		return letter+") " + answer + ": " + numClicked;
	}
	
	public String toJSON() {
		return "{\"pollID\": \"" + pollID + "\", \"letter\": \"" + letter + "\", \"answer\": \"" + answer + "\", \"numClicked\": " + numClicked + "}";
	}
	
	public static PollAnswer fromJSON(String pollAnswerJSON) {
		JSONObject pollAnswerJO = new JSONObject(pollAnswerJSON);
		String pollID = pollAnswerJO.getString("pollID");
		String letter = pollAnswerJO.getString("letter");
		String answer = pollAnswerJO.getString("answer");
		int numClicked = pollAnswerJO.getInt("numClicked");
		return new PollAnswer(pollID, letter, answer, numClicked);
	}
	
}
