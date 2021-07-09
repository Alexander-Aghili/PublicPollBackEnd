package com.rest.publicpoll.polls;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rest.publicpoll.CreateID;
import com.rest.publicpoll.Poll;
import com.rest.publicpoll.PollAnswer;
import com.rest.publicpoll.PollComment;

/*
 * Copyright © 2021 Alexander Aghili - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexander Aghili alexander.w.aghili@gmail.com, May 2021
 */



/*
 * AdjustPollDatabase Method Reference:
 * 
 * addNewPoll: Returns pollID in String
 * returnPollJSONFromID: Returns Poll in JSON format, refer to PollJSON for more info.
 * getRandomListOfPolls: Returns a random list of polls, right now its just in order and all of them
 * addOneCountToAnswer: Returns "ok"
 * addComment: Returns "ok" 
 * 
 */

public class AdjustPollDatabase 
{
	
	private static Connection connect = null;
	private static Statement statement = null;
	private static ResultSet resultSet = null;
	private static PreparedStatement preparedStatement = null;
	
	//Values are hidden, if using, put in appropriate username and password
	private static final String USERNAME = "root";
	private static final String PASSWORD = "alexWa0720";
	//Sql domain, in this case is the AWS, could be localhost.
	private static final String SQL_DOMAIN = "localhost:3306";
	
	private static final int ID_LENGTH = 10;
	private static final String ERROR_RESPONSE = "<title>HTTP Error 500 - Internal Service Error</title>";
	
	
	/*
	 * MySQL Table Reference:
	 * 
	 * polldb:
	 * 	polls:
	 * 		pollID: String
	 * 		pollQuestion: String
	 * 	answers:
	 * 		pollID: String
	 * 		letter: String
	 * 		answer: String
	 * 		totalClicked: INT
	 * 	comments:
	 * 		id: INT
	 * 		pollID: String
	 * 		comment: String
	 * 		user: String
	 */
	
	/*
	 * SQL Table Creation Statements:
	 * CREATE TABLE `comments` ( `id` int auto_increment not null, `pollID` varchar(400) NOT NULL,   `comment` varchar(1024) DEFAULT NULL, `user` varchar(1024) DEFAULT NULL,    PRIMARY KEY (`id`) )
	 * CREATE TABLE `polls` (   `id` varchar(400) NOT NULL,   `pollQuestion` varchar(1024) DEFAULT NULL,  `isPrivate` bool   PRIMARY KEY (`id`) );
	 * CREATE TABLE `answers` (`pollID` varchar (400) NOT NULL, `letter` varchar(16) NOT NULL, `answer` varchar (1024) DEFAULT NULL, `totalClicked` INT DEFAULT NULL, PRIMARY KEY (`pollID`, `letter`));
	 */
	
	
	
	
	/*
	 * 	Adds New Poll via following process:
	 * Takes in a poll with no PollID, answers with PollID.
	 * Randomly creates a 10 length ID, checks if the ID exists and if not makes the ID attached to the poll and enters the poll into the database.
	 * Also adds ID to all the poll answers.
	 * Adds poll answers into answers table in polldb
	 * Returns pollID
	 */
	public static String addNewPoll(Poll poll) {
		String response = ERROR_RESPONSE;
		if (!checkValidPoll(poll))
		{
			return "<title>Invalid Poll. Add information.</title>";
		}
		try {
			initializeDB();
			
			String pollID = CreateID.createID(ID_LENGTH, preparedStatement, connect, "polls", "pollID");
			addPollToDatabase(poll, pollID);
			addPollAnswersToDatabase(poll.getAnswers(), pollID);
			response = pollID;
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return response;
	}
	
	private static boolean checkValidPoll(Poll poll) {
		if (poll.getPollQuestion().equals("")) {
			return false;
		}
		for (int i = 0; i < poll.getAnswers().size(); i++) {
			if (poll.getAnswers().get(i).getAnswer().equals("")) {
				return false;
			}
		}
		return true;
	}
		
	//Can be expanded upon later
	private static void addPollToDatabase(Poll poll, String pollID) throws SQLException {
		preparedStatement = connect.prepareStatement("INSERT INTO polldb.polls VALUES(?, ?, ?)");
		preparedStatement.setString(1, pollID);
		preparedStatement.setString(2, poll.getPollQuestion());
		preparedStatement.setBoolean(3, poll.isPrivate());
		preparedStatement.executeUpdate();
	}
	
	private static void addPollAnswersToDatabase(ArrayList<PollAnswer> pollAnswers, String pollID) throws SQLException {
		//Goes through each answer to add into to database.
		for(PollAnswer answer: pollAnswers) {
			//1. pollID, 2. letter, 3. answer, 4. numClicked
			preparedStatement = connect.prepareStatement("INSERT INTO polldb.answers VALUES(?, ?, ?, ?)");
			preparedStatement.setString(1, pollID);
			preparedStatement.setString(2, answer.getLetter());
			preparedStatement.setString(3, answer.getAnswer());
			preparedStatement.setInt(4, answer.getNumClicked());
			preparedStatement.executeUpdate();
			
		}
	}
	
	//Calls getPollFromID and returns the JSON
	public static String returnPollJSONFromID(String pollID) {
		String response = "";
		try {
			initializeDB();
			response = getPollFromID(pollID).toJSON();
		} catch (ClassNotFoundException | SQLException e) {
			for (int i = 0; i < e.getStackTrace().length; i++) 
				response += e.getStackTrace()[i] + "\n";
		} catch (NoPollFoundException e) {
			response = "error";
		} finally {
			close();
		}
		return response;
	}
	
	/*
	 * Gets Poll from the ID via the following
	 * Gets the pollQuestion by searching sql database
	 * Gets all the pollAnswers by querying database
	 * Makes a new Poll and returns the JSON form
	 */
	private static Poll getPollFromID(String pollID) throws SQLException, NoPollFoundException {
		
		preparedStatement = connect.prepareStatement("SELECT * FROM polldb.polls WHERE pollID = ?");
		preparedStatement.setString(1, pollID);
		ResultSet results = preparedStatement.executeQuery();
		results.next();
		String pollQuestion = results.getString("pollQuestion"); 
		boolean isPrivate = results.getBoolean("isPrivate");
		
		if (pollQuestion.equals("")) { 
			throw new NoPollFoundException(pollID);
		}
		ArrayList<PollAnswer> answers = getPollAnswersFromID(pollID);
		ArrayList<PollComment> comments = getPollCommentsFromID(pollID);
		
		return new Poll(pollID, pollQuestion, answers, comments, isPrivate);
	}
	
	private static ArrayList<PollAnswer> getPollAnswersFromID(String pollID) throws SQLException {
		ArrayList<PollAnswer> answers = new ArrayList<PollAnswer>();
		preparedStatement = connect.prepareStatement("SELECT * FROM polldb.answers WHERE pollID = ?");
		preparedStatement.setString(1, pollID);
		ResultSet results = preparedStatement.executeQuery();
		while (results.next()) {
			String letter = results.getString("letter");
			String answerString = results.getString("answer");
			int numClicked = results.getInt("totalClicked");
			answers.add(new PollAnswer(letter, answerString, numClicked));
		}
		return answers;
	}
	
	private static ArrayList<PollComment> getPollCommentsFromID(String pollID) throws SQLException {
		ArrayList<PollComment> comments = new ArrayList<PollComment>();
		preparedStatement = connect.prepareStatement("SELECT * FROM polldb.comments WHERE pollID = ?");
		preparedStatement.setString(1, pollID);
		ResultSet results = preparedStatement.executeQuery();
		while (results.next()) {
			int commentID = results.getInt("id");
			String comment = results.getString("comment");
			String user = results.getString("user");
			comments.add(new PollComment(comment, user, commentID));
		}
		return comments;
	}
	
	
	/*
	 * Will be algorithmically taylored
	 * Implement
	 */
	public static String getRandomListOfPolls() {
		String response = "";
		try {
			initializeDB();
			//Just go through all
			preparedStatement = connect.prepareStatement("SELECT * FROM polldb.polls WHERE isPrivate = ?");
			preparedStatement.setBoolean(1, false);
			ResultSet results = preparedStatement.executeQuery();
			response = "{\"polls\": [";
			while (results.next()) {
				String pollID = results.getString("pollID");
				String pollJSON = getPollFromID(pollID).toJSON();
				response += pollJSON + ",";
			}
			response = response.substring(0, response.length() - 1);
			response += "]}";
		} catch (ClassNotFoundException | SQLException | NoPollFoundException e) {
			e.printStackTrace();
			for (int i = 0; i < e.getStackTrace().length; i++) 
				response += e.getStackTrace()[i] + "\n";
		} finally {
			close();
		}
		return response;
	}
	
	/*
	 * Im not really sure which way is faster, querying all of the polls
	 *  and getting the ones I need through a search on the server or
	 *  just querying one by one in the database. No clue, just gunna go with
	 *  database for now because its easier.
	 *  
	 * Query database with each pollID, get polls and put them in arraylist,
	 * make an jsonarray with the polls and return it in string form.
	 */
	public static String getPollsJSONFromPollIDs(ArrayList<String> pollIDs) {
		String response = "";
		try {
			ArrayList<Poll> polls = new ArrayList<Poll>();
			initializeDB();
			response = "{\"polls\": [";
			for (int i = 0; i < pollIDs.size(); i++) {
				String pollID = pollIDs.get(i);
				response += getPollFromID(pollID).toJSON() + ",";
			}
			response = response.substring(0, response.length() - 1);
			response += "]}";
		} catch (ClassNotFoundException | SQLException | NoPollFoundException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	public static String addOneCountToAnswer(PollAnswer pollAnswer) {
		String response = "";
		try {
			initializeDB();
			preparedStatement = connect.prepareStatement("UPDATE polldb.answers SET totalClicked = ? WHERE pollID = ? AND letter = ?;");
			preparedStatement.setInt(1, pollAnswer.getNumClicked()+1);
			preparedStatement.setString(2, pollAnswer.getID());
			preparedStatement.setString(3, pollAnswer.getLetter());
			preparedStatement.executeUpdate();
			response = "ok";
		} catch (ClassNotFoundException | SQLException e) {
			for (int i = 0; i < e.getStackTrace().length; i++) 
				response += e.getStackTrace()[i] + "\n";
		} finally {
			close();
		}
		return response;
	}
	
	
	public static String addComment(PollComment pollComment) {
		String response = "";
		try {
			initializeDB();
			// Add Comment
			preparedStatement = connect.prepareStatement("INSERT INTO polldb.comments VALUES(default, ?, ?, ?)");
			preparedStatement.setString(1, pollComment.getPollID());
			preparedStatement.setString(2, pollComment.getComment());
			preparedStatement.setString(3, pollComment.getUser());
			preparedStatement.executeUpdate();
			response = "ok";
		} catch (ClassNotFoundException | SQLException e) {
			for (int i = 0; i < e.getStackTrace().length; i++) 
				response += e.getStackTrace()[i] + "\n";
		} finally {
			close();
		}
		return response;
	}
	
	public static String deletePoll(Poll poll) {
		String response = "";
		try {
			initializeDB();
			deletePollInfo(poll, "polls");
			deletePollInfo(poll, "answers");
			deletePollInfo(poll, "comments");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			for (int i = 0; i < e.getStackTrace().length; i++) 
				response += e.getStackTrace()[i] + "\n";
		} finally {
			close();
		}
		return response;
	}
	
	public static void deletePollInfo(Poll poll, String table) throws SQLException {
		preparedStatement = connect.prepareStatement("DELETE FROM polldb." + table + " WHERE pollID = ?");
		preparedStatement.setString(1, poll.getPollID());
		preparedStatement.executeUpdate();
	}
	
	public static String deleteComment(String commentIDString) {
		String response = "";
		try {
			initializeDB();
			int id = Integer.parseInt(commentIDString);
			preparedStatement = connect.prepareStatement("DELETE FROM polldb.comments WHERE id = ?");
			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
			response = "ok";
		} catch (Exception e) {
			e.printStackTrace();
			response = "error";
		} finally {
			close();
		}
		return response;
	}
	
	//All methods must init the DB when starting up to establish a connection.
	private static void initializeDB() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		connect = DriverManager.getConnection("jdbc:mysql://" + SQL_DOMAIN + "/polldb", USERNAME, PASSWORD);
		statement = connect.createStatement();
	}
	
	//All must close the DB at the end to ensure no leakage.
	private static void close() {
		try {
			if(resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connect != null) {
				connect.close();
			}
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	
}
