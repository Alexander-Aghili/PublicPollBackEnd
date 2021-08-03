package com.rest.publicpoll;

/*
 * Copyright © 2021 Alexander Aghili - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexander Aghili alexander.w.aghili@gmail.com, May 2021
 */

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 * TODO:
 * - Comments: Add UserID for user
 * - Answers: Add UserIDs in list for each answer
 */

/*
 * Poll:
 * pollID: String. Contains the pollID to reference in database.
 * pollQuestion: String. Contains the question of the poll.
 * answers: ArrayList<PollAnswer>. Contains a list of all the answers.
 * comments: ArrayList<PollComment>. Contains a list of all the comments.
 */

public class Poll {

	private String creatorID;
	private String pollID;
	private String pollQuestion;
	private ArrayList<PollAnswer> answers;
	private ArrayList<PollComment> comments;
	private boolean isPrivate;
	
	//All info available
	public Poll(String creatorID, String pollID, String pollQuestion, ArrayList<PollAnswer> answers, ArrayList<PollComment> comments, boolean isPrivate) {
		this.creatorID = creatorID;
		this.pollID = pollID;
		this.pollQuestion = pollQuestion;
		this.answers = answers;
		this.comments = comments;
		this.isPrivate = isPrivate;
	}

	//First created, no pollID and no comments.
	public Poll(String creatorID, String pollQuestion, ArrayList<PollAnswer> answers, boolean isPrivate) {
		this.creatorID = creatorID;
		this.pollQuestion = pollQuestion;
		this.answers = answers;
		this.comments = new ArrayList<PollComment>();
		this.isPrivate = isPrivate;
	}

	public String getCreatorID() {
		return creatorID;
	}

	public void setCreatorID(String creatorID) {
		this.creatorID = creatorID;
	}

	public ArrayList<PollComment> getComments() {
		return comments;
	}

	public void setComments(ArrayList<PollComment> comments) {
		this.comments = comments;
	}

	public String getPollID() {
		return pollID;
	}


	public void setPollID(String pollID) {
		this.pollID = pollID;
	}


	public String getPollQuestion() {
		return pollQuestion;
	}


	public void setPollQuestion(String pollQuestion) {
		this.pollQuestion = pollQuestion;
	}


	public ArrayList<PollAnswer> getAnswers() {
		return answers;
	}


	public void setAnswers(ArrayList<PollAnswer> answers) {
		this.answers = answers;
	}
	
	public String toJSON() {
		String poll = "{\"poll\":\"" + pollID + "\",\"question\":\"" + pollQuestion + "\",\"answers\": [";
		for (int i = 0; i < answers.size(); i++) {
			PollAnswer answer = answers.get(i);
			String userIDsJSON = "";
			for (int j = 0; j < answer.getUserIDs().size(); j++) {
				userIDsJSON += "\"" + answer.getUserIDs().get(j) + "\",";
			}
			
			if (userIDsJSON.length() > 0) 
				userIDsJSON = userIDsJSON.substring(0, userIDsJSON.length() - 1);
			
			String pollAnswerJSON = "   {\"letter\": \"" + answer.getLetter() + "\","
					+ "\"answer\": \"" + answer.getAnswer() + "\","
					+ "\"users\":[" + userIDsJSON + "]},";
			if (i == answers.size() - 1) {
				poll += pollAnswerJSON.substring(0, pollAnswerJSON.length() - 1) + "";
			} else {
				poll += pollAnswerJSON;
			}
		}
		poll += " ],\"comments\": [";
		
		for (int i = 0; i < comments.size(); i++) {
			PollComment comment = comments.get(i);
			String pollCommentJSON = "   {\"user\": \"" + comment.getUser() + "\","
					+ "\"comment\": \"" + comment.getComment() + "\","
					+ "\"commentID\": " + comment.getCommentID() + "},";
			if (i == comments.size() - 1) {
				poll += pollCommentJSON.substring(0, pollCommentJSON.length() - 1) + "\n";
			} else {
				poll += pollCommentJSON;
			}
		}
		poll += " ], \"isPrivate\": " + isPrivate + ", \"creatorID\": \"" + creatorID + "\"}";
		return poll;
	}
	
	public static Poll fromJSON(String pollJSON) {
		JSONObject pollJO = new JSONObject(pollJSON);
		String creatorID = (String) pollJO.get("creatorID");
		String pollID = (String) pollJO.get("poll");
		String pollQuestion = (String) pollJO.get("question");
		ArrayList<PollAnswer> answers = getAnswersFromJSON(pollJO, pollID);
		ArrayList<PollComment> comments = getCommentsFromJSON(pollJO, pollID);
		boolean isPrivate = pollJO.getBoolean("isPrivate");
		return new Poll(creatorID, pollID, pollQuestion, answers, comments, isPrivate);
	}
	
	public static Poll fromJSONCreation(String pollJSON) {
		JSONObject pollJO = new JSONObject(pollJSON);
		String creatorID = (String) pollJO.get("creatorID");
		String pollQuestion = (String) pollJO.get("question");
		ArrayList<PollAnswer> answers = getAnswersFromJSON(pollJO);
		boolean isPrivate = pollJO.getBoolean("isPrivate");
		return new Poll(creatorID, pollQuestion, answers, isPrivate);
	}
	
	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	private static ArrayList<PollAnswer> getAnswersFromJSON(JSONObject pollJSON, String pollID) {
		ArrayList<PollAnswer> answersList = new ArrayList<PollAnswer>();
		JSONArray answersJA = (JSONArray) pollJSON.get("answers");
		for(int i = 0; i < answersJA.length(); i++) {
			JSONObject answerJSON = answersJA.getJSONObject(i);
			
			
			answersList.add(PollAnswer.fromJSON(answerJSON.toString()));
		}
		
		return answersList;
	}
	
	//For creation
	private static ArrayList<PollAnswer> getAnswersFromJSON(JSONObject pollJSON) {
		ArrayList<PollAnswer> answersList = new ArrayList<PollAnswer>();
		JSONArray answersJA = (JSONArray) pollJSON.get("answers");
		for(int i = 0; i < answersJA.length(); i++) {
			JSONObject answerJSON = answersJA.getJSONObject(i);
			String letter = answerJSON.getString("letter");
			String answer = answerJSON.getString("answer");
			JSONArray ja = answerJSON.getJSONArray("users");
			
			ArrayList<String> userIDs = new ArrayList<String>();
			for (int k = 0; k < ja.length(); k++) {
				userIDs.add(ja.getString(k));
			}
			answersList.add(new PollAnswer(letter, answer, userIDs));
		}
		
		return answersList;
	}
	
	private static ArrayList<PollComment> getCommentsFromJSON(JSONObject pollJSON, String pollID) {
		ArrayList<PollComment> commentsList = new ArrayList<PollComment>();
		JSONArray commentsJA = (JSONArray) pollJSON.get("comments");
		for (int i = 0; i < commentsJA.length(); i++) {
			JSONObject commentJSON = commentsJA.getJSONObject(i);
			//Replace with user object later
			String user = commentJSON.getString("user");
			
			String comment = commentJSON.getString("comment");
			int commentID = commentJSON.getInt("commentID");
			commentsList.add(new PollComment(pollID, comment, user, commentID));
		}
		return commentsList;
	}
	
	@Override
	public String toString() {
		String poll = "{\"poll\" : \"" + pollID + "\", \"question\" : \"" + pollQuestion + "\"}";
		return poll;
	}
	

}
