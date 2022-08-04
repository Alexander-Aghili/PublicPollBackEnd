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
import java.sql.Timestamp;
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
	
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private PreparedStatement preparedStatement = null;
	
	//Values are hidden, if using, put in appropriate username and password
	private final String USERNAME = "root";
	private final String PASSWORD = "*******";
	private final String SQL_DOMAIN = "localhost:3306";
	
	private final int ID_LENGTH = 12;
	
	
	
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
		4 - savedPolls
		2 - recentlyRespondedToPolls
		3 - myPolls
		
		
		create table userPolls(
			`userID` varchar(400),
			`pollID` varchar(400),
			`type` INT,
			`timeRef` DATETIME,
		);
		
	 */
	public AdjustUsersDatabase() {}
	public AdjustUsersDatabase(boolean multipleRequests) {
		try {
			initializeDB();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		//Make sure to close outside yourself if you are doing a multirequest type instance
	}
	
	public String createNewUser(User user) {
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
	
	private void addUserToDatabase(User user, String id) throws SQLException {
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
	
	public String verifyUserData(String email, String username) {
		String response = "";
		try {
			initializeDB();
			if (email != "") checkExists("email", email);
			if (username != "") checkExists("username", username);
			response = "ok";
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
	public void checkExists(String parameter, String data) throws SQLException, UserDataExistsException { 
		preparedStatement = connect.prepareStatement("SELECT * FROM usersdb.users WHERE " + parameter + " = ?");
 		preparedStatement.setString(1, data);
		ResultSet results = preparedStatement.executeQuery();
		if (results.isBeforeFirst())
			throw new UserDataExistsException(parameter);
	}
	
	public String editUserData(String userID, String key, Object value) {
		String response = "";
		try {
			initializeDB();
			preparedStatement = connect.prepareStatement("UPDATE users SET " + key + " = ? WHERE id = ?");
			preparedStatement.setObject(1, value);
			preparedStatement.setString(2, userID);
			preparedStatement.executeUpdate();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return response;
	}
	
	public void editUserDataMultiRequest(String userID, String key, Object value) {
		try {
			preparedStatement = connect.prepareStatement("UPDATE users SET " + key + " = ? WHERE id = ?");
			preparedStatement.setObject(1, value);
			preparedStatement.setString(2, userID);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public String savePoll(String table, String userID, String pollID) {
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
	public String signInWithUsernameAndPassword(String username, String password) {
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
	public String getUserJSONByID(String id) {
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
	private User getUserByID(String id) throws SQLException {
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
	
	public String getUsersJSONByIDs(ArrayList<String> uids) {
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
	
	public String addUserPoll(String userID, String pollID, int type) {
		String response = "";
		try {
			initializeDB();
			boolean exists = checkUserPollExists(userID, pollID, type);
			
			if (exists)
				throw new UserDataExistsException("UserPollType");
			System.out.println(userID + " " + pollID + " " + type);
			preparedStatement = connect.prepareStatement("INSERT INTO usersdb.userpolls VALUES(?, ?, ?, ?)");
			preparedStatement.setString(1, userID);
			preparedStatement.setString(2, pollID);
			preparedStatement.setInt(3, type);
			preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			preparedStatement.executeUpdate();
			response = "ok";
		} catch (ClassNotFoundException | SQLException | UserDataExistsException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return response;
	}
	
	private boolean checkUserPollExists(String userID, String pollID, int type) throws SQLException {
		preparedStatement = connect.prepareStatement("SELECT * FROM usersdb.userpolls WHERE pollID = ? AND userID = ? AND type = ?");
		preparedStatement.setString(1, pollID);
		preparedStatement.setString(2, userID);
		preparedStatement.setInt(3, type);
		ResultSet results = preparedStatement.executeQuery();
		if (results.isBeforeFirst()) {
			return true;
		}
		return false;
	}
	
	public String deletePollFromUserPolls(String id) {
		String response = "";
		try {
			initializeDB();
			preparedStatement = connect.prepareStatement("DELETE FROM userpolls WHERE pollID = ?");
			preparedStatement.setString(1, id);
			preparedStatement.executeUpdate();
			response = "ok";
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return response;
	}
	
	public String checkUserPollExistsResponse(String userID, String pollID, int type) {
		String response = "false";
		try {
			initializeDB();
			boolean exists = checkUserPollExists(userID, pollID, type+3);
			if (exists) response = "true";
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return response;
	}
	
	public String deleteUserPoll(String userID, String pollID, int type) {
		String response = "";
		try {
			initializeDB();
			preparedStatement = connect.prepareStatement("DELETE FROM userpolls WHERE pollID = ? AND userID = ? AND type = ?");
			preparedStatement.setString(1, pollID);
			preparedStatement.setString(2, userID);
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
	
	public String deleteUser(String userID) {
		String response = "";
		try {
			initializeDB();
			preparedStatement = connect.prepareStatement("DELETE FROM usersdb.userpolls WHERE userID = ?");
			preparedStatement.setString(1, userID);
			preparedStatement.executeUpdate();
			
			//Maybe delete from poll?
			
			preparedStatement = connect.prepareStatement("DELETE FROM usersdb.users WHERE userID = ?");
			preparedStatement.setString(1, userID);
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
	private void initializeDB() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		connect = DriverManager.getConnection("jdbc:mysql://" + SQL_DOMAIN + "/usersdb", USERNAME, PASSWORD);
		statement = connect.createStatement();
	}
	
	//All must close the DB at the end to ensure no leakage.
	public void close() {
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
