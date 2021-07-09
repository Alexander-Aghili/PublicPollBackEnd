package com.rest.publicpoll.users;
/*
 * Copyright © 2021 Alexander Aghili - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexander Aghili alexander.w.aghili@gmail.com, June 2021
 */

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.rest.publicpoll.BirthDay;
import com.rest.publicpoll.CreateID;
import com.rest.publicpoll.User;


/*
 * TODO:
 * Edit user info
 * Delete user
 * Remove saved poll
 * 
 */
public class AdjustUsersDatabase 
{
	
	private static Connection connect = null;
	private static Statement statement = null;
	private static ResultSet resultSet = null;
	private static PreparedStatement preparedStatement = null;
	
	//Values are hidden, if using, put in appropriate username and password
	private static final String USERNAME = "root";
	private static final String PASSWORD = "alexWa0720";
	private static final String SQL_DOMAIN = "localhost:3306";
	
	private static final int ID_LENGTH = 12;
	
	
	/*
	 * SQL Creation Statements:
	 
		create table users(
			`id` varchar(400) NOT NULL,
		    `username` varchar(1024),
		    `email` varchar(1024),
		    `firstname` varchar(1024),
		    `lastname` varchar(1024),
		    `birthday` Date,
		    `password` varchar(1024),
		    `gender` varchar(1024),
		    `profilePicture` varchar(1024),
		    primary key(`id`)
		);
		
		type KEY:
		1 - savedPolls
		2 - recentlyRespondedToPolls
		3 - myPolls
		
		
		create table userPolls(
			`userID` varchar(400),
			`pollID` varchar(400),
			`type` INT
		);
		
	 */
	
	public static String createNewUser(User user) {
		String response = "";
		
		try {
			initializeDB();
			String id = CreateID.createID(ID_LENGTH, preparedStatement, connect, "users", "id");
			addUserToDatabase(user, id);
			response = id;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return response;
	}
	
	private static void addUserToDatabase(User user, String id) throws SQLException {
		preparedStatement = connect.prepareStatement("INSERT INTO usersdb.users VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
		preparedStatement.setString(1, id);
		preparedStatement.setString(2, user.getUsername());
		preparedStatement.setString(3, user.getEmail());
		preparedStatement.setString(4, user.getFirstname());
		preparedStatement.setString(5, user.getLastname());
		preparedStatement.setDate(6, user.getBirthday().toSQLDate());
		preparedStatement.setString(7, user.getEncryptedPassword());
		preparedStatement.setString(8, user.getGender());
		preparedStatement.setString(9, user.getProfilePicture());
		preparedStatement.executeUpdate();
	}
	
	public static String verifyUserData(String email, String username) {
		String response = "ok";
		try {
			initializeDB();
			checkExists("email", email);
			checkExists("username", username);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} catch (UserDataExistsException e) {
			response = e.getIssueParameter();
		} finally {
			close();
		}
		
		return response;
	}
	
	//Throws DataExistsException is data exists, otherwise leaves the method.	
	private static void checkExists(String parameter, String data) throws SQLException, UserDataExistsException { 
		preparedStatement = connect.prepareStatement("SELECT * FROM usersdb.users WHERE " + parameter + " = ?");
 		preparedStatement.setString(1, data);
		ResultSet results = preparedStatement.executeQuery();
		if (results.isBeforeFirst())
			throw new UserDataExistsException(parameter);
	}
	
	
	public static String savePoll(String table, String userID, String pollID) {
		String response = "";
		
		try {
			initializeDB();
			preparedStatement = connect.prepareStatement("INSERT INTO usersdb." + table + " VALUES(?, ?)");
			preparedStatement.setString(1, userID);
			preparedStatement.setString(2, pollID);
			preparedStatement.executeUpdate();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return response;
	}
	
	//Returns "info error" if error is with username or password
	//Returns "regular error" if server error
	//Returns id is no errors.
	public static String signInWithUsernameAndPassword(String username, String password) {
		String response = "";
		try {
			initializeDB();
			preparedStatement = connect.prepareStatement("SELECT * FROM usersdb.users WHERE username = ?");
			preparedStatement.setString(1, username);
			ResultSet results = preparedStatement.executeQuery();
			if (!results.isBeforeFirst()) {
				response = "info error";
				return response;
			} 
		
			
			results.next();
			String hashedPasswordFromDatabase = results.getString("password");
			
			if (!PasswordEncryption.validatePassword(password, hashedPasswordFromDatabase)) {
				response = "info error";
				return response;
			}
			
			response = results.getString("id");
		
		} catch (ClassNotFoundException | SQLException e) {
			response = "regular error";
			e.printStackTrace();
		} finally {
			close();
		}
		
		return response;
	}
	
	
	/* WARNING: Read at your own risk, you may become dumber as you continue to read
	 * 
	 * This one is going to operate a bit stangely since we store only the pollIDs of the polls in the tables
	 * So basically instead of querying for all the polls for the user in this request,
	 * these queries will just collect all of the data that it needs for the user.
	 * 
	 * This means the flow for going on a users account page is as follows:
	 * - Request for User from UID
	 * - Query for User main information from UID.
	 * - Query for User poll information from UID.
	 * - Return user object in JSON format, polls are only the pollID
	 * - User receives the JSON, creates User object and displays information on their side, 
	 * - User also receives pollID's and querys for those pollIDs
	 * - User receives the polls in JSON and constructs objects and displays information as needed
	 * 
	 * Benefits:
	 * - If querying in a search bar or showing someone in a comment for example,
	 * 	 	you don't recieve all of the poll information for everyone even if you don't click on them,
	 * 		instead, only when clicked on can the other request be made. This speeds up the process of data.
	 * 	
	 * Drawbacks:
	 * - Multiple requests for data that could be sent once.
	 * 
	 */
	public static String getUserJSONByID(String id) {
		String response = "";
		try {
			initializeDB();
			response = getUserByID(id).toJSON();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return response;
	}
	
	//Maybe make NoUserFoundException and do the same thing the NoPollFoundException does...
	private static User getUserByID(String id) throws SQLException {
		ArrayList<String> savedPolls = new ArrayList<String>();
		ArrayList<String> recentPolls = new ArrayList<String>();
		ArrayList<String> myPolls = new ArrayList<String>();
		
		preparedStatement = connect.prepareStatement("SELECT * FROM usersdb.users WHERE id = ?");
		preparedStatement.setString(1, id);
		ResultSet results = preparedStatement.executeQuery();
		results.next();
		String username = results.getString("username");
		String email = results.getString("email");
		
		Date sqlDate = (Date) results.getObject("birthday"); 
		BirthDay birthday = new BirthDay(sqlDate);
		
		String firstname = results.getString("firstname");
		String lastname = results.getString("lastname");
		String gender = results.getString("gender");
		String profilePictureLink = results.getString("profilePicture");
		
		preparedStatement = connect.prepareStatement("SELECT * FROM usersdb.userpolls WHERE userID = ?");
		preparedStatement.setString(1, id);
		results = preparedStatement.executeQuery();
		
		while(results.next()) {
			int type = results.getInt("type");
			String pollID = results.getString("pollID");
			if (type == 1) {
				savedPolls.add(pollID);
			} else if (type == 2) {
				recentPolls.add(pollID);
			} else {
				myPolls.add(pollID);
			}
		}
		
		return new User(id, username, email, birthday, firstname, lastname, gender, profilePictureLink, savedPolls, recentPolls, myPolls);
	}
	
	public static String getUsersJSONByIDs(ArrayList<String> uids) {
		String response = "";
		try {
			initializeDB();
			response = "{\"users\": [";
			for (int i = 0; i < uids.size(); i++) {
				response += getUserJSONByID(uids.get(i)) + ",";
			}
			response = response.substring(0, response.length() - 1) + "]}";
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return response;

		
	}
	
	public static String addUserPolls(String userID, String pollID, int type) {
		String response = "";
		
		try {
			initializeDB();
			preparedStatement = connect.prepareStatement("INSERT INTO usersdb.userpolls VALUES(?, ?, ?)");
			preparedStatement.setString(1, userID);
			preparedStatement.setString(2, pollID);
			preparedStatement.setInt(3, type);
			preparedStatement.executeUpdate();
			response = "ok";
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return response;
	}
	
	//All methods must init the DB when starting up to establish a connection.
	private static void initializeDB() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		connect = DriverManager.getConnection("jdbc:mysql://" + SQL_DOMAIN + "/usersdb", USERNAME, PASSWORD);
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
