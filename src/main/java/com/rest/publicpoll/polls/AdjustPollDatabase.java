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
import com.rest.publicpoll.users.AdjustUsersDatabase;

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
	
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private PreparedStatement preparedStatement = null;
	
	//Values are hidden, if using, put in appropriate username and password
	private final String USERNAME = "root";
	private final String PASSWORD = "alexWa0720";
	//Sql domain, in this case is the AWS, could be localhost.
	private final String SQL_DOMAIN = "localhost:3306";
	
	private final int ID_LENGTH = 10;
	private final String ERROR_RESPONSE = "<title>HTTP Error 500 - Internal Service Error</title>";
	
	
	/*
	 * MySQL Table Reference:
	 * 
	 * polldb:
	 * 	polls:
	 * 		pollID: String
	 * 		pollQuestion: String
	 * 		isPrivate: boolean
	 * 		creatorID: String
	 * 	answers:
	 * 		pollID: String
	 * 		letter: String
	 * 		answer: String
	 * 	comments:
	 * 		id: INT
	 * 		pollID: String
	 * 		comment: String
	 * 		user: String
	 * 	userResponses:
	 * 		userID: String
	 * 		pollID: String
	 * 		letter: String
	 */
	
	/*
	 * SQL Table Creation Statements:
	 * CREATE TABLE `comments` ( `id` int auto_increment not null, `pollID` varchar(400) NOT NULL,   `comment` varchar(1024) DEFAULT NULL, `user` varchar(1024) DEFAULT NULL,    PRIMARY KEY (`id`) )
	 * CREATE TABLE `polls` (   `id` varchar(400) NOT NULL,   `pollQuestion` varchar(1024) DEFAULT NULL,  `isPrivate` bool, `creatorID` varchar(400)   PRIMARY KEY (`id`) );
	 * CREATE TABLE `answers` (`pollID` varchar (400) NOT NULL, `letter` varchar(16) NOT NULL, `answer` varchar (1024) DEFAULT NULL, `totalClicked` INT DEFAULT NULL, PRIMARY KEY (`pollID`, `letter`));
	 * CREATE TABLE userResponses (`userID` varchar(400), `pollID` varchar(400), `letter` varchar(16));
	 */
	
	
	
	
	/*
	 * 	Adds New Poll via following process:
	 * Takes in a poll with no PollID, answers with PollID.
	 * Randomly creates a 10 length ID, checks if the ID exists and if not makes the ID attached to the poll and enters the poll into the database.
	 * Also adds ID to all the poll answers.
	 * Adds poll answers into answers table in polldb
	 * Returns pollID
	 */
	public String addNewPoll(Poll poll) {
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
	
	private boolean checkValidPoll(Poll poll) {
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
	private void addPollToDatabase(Poll poll, String pollID) throws SQLException {
		preparedStatement = connect.prepareStatement("INSERT INTO polldb.polls VALUES(?, ?, ?, ?)");
		preparedStatement.setString(1, pollID);
		preparedStatement.setString(2, poll.getPollQuestion());
		preparedStatement.setBoolean(3, poll.isPrivate());
		preparedStatement.setString(4, poll.getCreatorID());
		preparedStatement.executeUpdate();
	}
	
	private void addPollAnswersToDatabase(ArrayList<PollAnswer> pollAnswers, String pollID) throws SQLException {
		//Goes through each answer to add into to database.
		for(PollAnswer answer: pollAnswers) {
			//1. pollID, 2. letter, 3. answer
			preparedStatement = connect.prepareStatement("INSERT INTO polldb.answers VALUES(?, ?, ?)");
			preparedStatement.setString(1, pollID);
			preparedStatement.setString(2, answer.getLetter());
			preparedStatement.setString(3, answer.getAnswer());
			preparedStatement.executeUpdate();
			
		}
	}
	
	//Calls getPollFromID and returns the JSON
	public String returnPollJSONFromID(String pollID) {
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
	private Poll getPollFromID(String pollID) throws SQLException, NoPollFoundException {
		
		preparedStatement = connect.prepareStatement("SELECT * FROM polldb.polls WHERE pollID = ?");
		preparedStatement.setString(1, pollID);
		ResultSet results = preparedStatement.executeQuery();
		results.next();
		String pollQuestion = results.getString("pollQuestion"); 
		boolean isPrivate = results.getBoolean("isPrivate");
		String creatorID = results.getString("creatorID");
		
		if (pollQuestion.equals("")) { 
			throw new NoPollFoundException(pollID);
		}
		ArrayList<PollAnswer> answers = getPollAnswersFromID(pollID);
		ArrayList<PollComment> comments = getPollCommentsFromID(pollID);
		
		return new Poll(creatorID, pollID, pollQuestion, answers, comments, isPrivate);
	}
	
	private ArrayList<PollAnswer> getPollAnswersFromID(String pollID) throws SQLException {
		ArrayList<PollAnswer> answers = new ArrayList<PollAnswer>();
		preparedStatement = connect.prepareStatement("SELECT * FROM polldb.answers WHERE pollID = ?");
		preparedStatement.setString(1, pollID);
		ResultSet results = preparedStatement.executeQuery();

		preparedStatement = connect.prepareStatement("SELECT * FROM polldb.userresponses WHERE pollID = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		preparedStatement.setString(1, pollID);
		ResultSet userIDResults = preparedStatement.executeQuery();
		while (results.next()) {
			String letter = results.getString("letter");
			String answerString = results.getString("answer");
			
			ArrayList<String> userIDs = new ArrayList<String>();
			
			while(userIDResults.next()) {
				if (userIDResults.getString("letter").equals(letter)) {
					userIDs.add(userIDResults.getString("userID"));
				}
			}
			userIDResults.beforeFirst();
			
			answers.add(new PollAnswer(letter, answerString, userIDs));
		}
		return answers;
	}
	
	private ArrayList<PollComment> getPollCommentsFromID(String pollID) throws SQLException {
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
	
	
	public String getRandomListOfPolls(String userID) {
		String response = "";
		try {
			initializeDB();
			ArrayList<String> userAlreadyRespondedPollIDs = new ArrayList<String>();
			
			preparedStatement = connect.prepareStatement("SELECT * FROM polldb.userresponses WHERE userID = ?");
			preparedStatement.setString(1, userID);
			ResultSet userRespondedPolls = preparedStatement.executeQuery();
			
			while(userRespondedPolls.next()) {
				userAlreadyRespondedPollIDs.add(userRespondedPolls.getString("pollID"));
			}
			
			preparedStatement = connect.prepareStatement("SELECT * FROM polldb.polls WHERE isPrivate = ? LIMIT 20");
			preparedStatement.setBoolean(1, false);
			ResultSet results = preparedStatement.executeQuery();
			response = "{\"polls\": [";
			while (results.next()) {
				String pollID = results.getString("pollID");
				if(!userAlreadyRespondedPollIDs.contains(pollID)) {
					String pollJSON = getPollFromID(pollID).toJSON();
					response += pollJSON + ",";	
				}
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
	public String getPollsJSONFromPollIDs(ArrayList<String> pollIDs) {
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
	
	
	public String addComment(PollComment pollComment) {
		String response = "";
		try {
			initializeDB();
			// Add Comment
			preparedStatement = connect.prepareStatement("INSERT INTO polldb.comments VALUES(default, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, pollComment.getPollID());
			preparedStatement.setString(2, pollComment.getComment());
			preparedStatement.setString(3, pollComment.getUser());
			preparedStatement.executeUpdate();
			
			try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	            	int id = generatedKeys.getInt(1);
	            	response = String.valueOf(id);
	            }
	            else {
	                throw new SQLException("No ID obtained.");
	            }
	        }
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			for (int i = 0; i < e.getStackTrace().length; i++) 
				response += e.getStackTrace()[i] + "\n";
		} finally {
			close();
		}
		return response;
	}
	
	public String deletePoll(String pollID) {
		String response = "";
		try {
			initializeDB();
			deletePollInfo(pollID, "polls");
			deletePollInfo(pollID, "answers");
			deletePollInfo(pollID, "comments");
			deletePollInfo(pollID, "userresponses");
			AdjustUsersDatabase usersDB = new AdjustUsersDatabase();
			if (usersDB.deletePollFromUserPolls(pollID).equals("ok"))
				response = "ok";
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			for (int i = 0; i < e.getStackTrace().length; i++) 
				response += e.getStackTrace()[i] + "\n";
		} finally {
			close();
		}
		return response;
	}
	
	public void deletePollInfo(String pollID, String table) throws SQLException {
		preparedStatement = connect.prepareStatement("DELETE FROM polldb." + table + " WHERE pollID = ?");
		preparedStatement.setString(1, pollID);
		preparedStatement.executeUpdate();
	}
	
	public String deleteComment(String commentIDString) {
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
	
	public String addUserResponseToPoll(String pollID, String userID, String letter) {
		String response = "";
		try {
			initializeDB();
			if (!userAnsweredAlready(pollID, userID)) {
				addResponse(pollID, userID, letter);
				response = "ok";
			} else {
				response = "duplicate";
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return response;
	}
	
	private boolean userAnsweredAlready(String pollID, String userID) throws SQLException {
		preparedStatement = connect.prepareStatement("SELECT * FROM polldb.userresponses WHERE userID = ? AND pollID = ?");
		preparedStatement.setString(1, userID);
		preparedStatement.setString(2, pollID);
		ResultSet results = preparedStatement.executeQuery();
		if (results.isBeforeFirst()) return true;
		return false;
		
	}
	
	private void addResponse(String pollID, String userID, String letter) throws SQLException {
		preparedStatement = connect.prepareStatement("INSERT INTO polldb.userresponses VALUES(?, ?, ?)");
		preparedStatement.setString(1, userID);
		preparedStatement.setString(2, pollID);
		preparedStatement.setString(3, letter);
		preparedStatement.executeUpdate();
	}
	
	//All methods must init the DB when starting up to establish a connection.
	private void initializeDB() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		connect = DriverManager.getConnection("jdbc:mysql://" + SQL_DOMAIN + "/polldb", USERNAME, PASSWORD);
		statement = connect.createStatement();
	}
	
	//All must close the DB at the end to ensure no leakage.
	private void close() {
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
