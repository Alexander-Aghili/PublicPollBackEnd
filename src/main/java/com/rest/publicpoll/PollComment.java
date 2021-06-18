package com.rest.publicpoll;

import org.json.JSONObject;

/*
 * Copyright © 2021 Alexander Aghili - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexander Aghili alexander.w.aghili@gmail.com, May 2021
 */


public class PollComment {

	private String pollID;
	private int commentID;
	private String comment;
	//For now is string anon, will be a user object in the future
	private String user;
	
	public PollComment(String comment) {
		this.comment = comment;
		user = "Anon";
	}
	public PollComment(String comment, String user) {
		this.comment = comment;
		this.user = user;
	}
	
	public PollComment(String comment, String user, int commentID) {
		this.comment = comment;
		this.user = user;
		this.commentID = commentID;
	}

	public PollComment(String pollID, String comment, String user) {
		this.pollID = pollID;
		this.comment = comment;
		this.user = user;
	}
	
	public PollComment(String pollID, String comment, String user, int commentID) {
		this.pollID = pollID;
		this.comment = comment;
		this.user = user;
		this.commentID = commentID;
	}
	
	public int getCommentID() {
		return commentID;
	}
	public void setCommentID(int commentID) {
		this.commentID = commentID;
	}
	

	public String getPollID() {
		return pollID;
	}

	public void setPollID(String pollID) {
		this.pollID = pollID;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public String toJSON() {
		return "{ \"pollID\": \"" + pollID + "\", \"comment\": \"" + comment + "\", \"user\": \"" + user + "\", \"commentID\": " + commentID + " }";
	}
	
	public static PollComment fromJSON(String pollCommentJSON) {
		JSONObject pollCommentJO = new JSONObject(pollCommentJSON);
		String pollID = pollCommentJO.getString("pollID");
		String comment = pollCommentJO.getString("comment");
		String user = pollCommentJO.getString("user");
		int commentID = pollCommentJO.getInt("commentID");
		return new PollComment(pollID, comment, user, commentID);
	}
	
}
