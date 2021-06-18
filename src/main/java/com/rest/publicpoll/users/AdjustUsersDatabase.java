package com.rest.publicpoll.users;
/*
 * Copyright © 2021 Alexander Aghili - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexander Aghili alexander.w.aghili@gmail.com, June 2021
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		    primary key(`id`)
		);
		
		create table savedPolls(
			`userID` varchar(400),
		    `pollID` varchar(400)
		);
		
		create table recentPolls(
			`userID` varchar(400),
		    `pollID` varchar(400),
		);
		
	 */
	
	public static String createNewUser(User user) {
		String response = "";
		
		try {
			initializeDB();
			String id = CreateID.createID(ID_LENGTH, preparedStatement, connect, "users", "id");
			checkExists("username", user.getUsername());
			checkExists("email", user.getEmail());
			addUserToDatabase(user, id);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} catch (DataExistsException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return response;
	}

	//Throws DataExistsException is data exists, otherwise leaves the method.	
	private static void checkExists(String parameter, String data) throws SQLException, DataExistsException { 
		preparedStatement = connect.prepareStatement("SELECT * FROM usersdb.users WHERE ? = ?");
		preparedStatement.setString(1, parameter);
		preparedStatement.setString(2, data);
		ResultSet results = preparedStatement.executeQuery();
		if (results.isBeforeFirst())
			throw new DataExistsException(parameter);
	}
	
	private static void addUserToDatabase(User user, String id) throws SQLException {
		preparedStatement = connect.prepareStatement("INSERT INTO usersdb.users VALUES(?, ?, ?, ?, ?, ?, ?)");
		preparedStatement.setString(1, id);
		preparedStatement.setString(2, user.getUsername());
		preparedStatement.setString(3, user.getEmail());
		preparedStatement.setString(4, user.getFirstname());
		preparedStatement.setString(5, user.getLastname());
		preparedStatement.setDate(6, user.getBirthday().toSQLDate());
		preparedStatement.setString(7, user.getEncryptedPassword());
		preparedStatement.executeUpdate();
	}
	
	public static String savePoll(String table, String userID, String pollID) {
		String response = "";
		
		try {
			initializeDB();
			preparedStatement = connect.prepareStatement("INSERT INTO usersdb.? VALUES(?, ?)");
			preparedStatement.setString(1, table);
			preparedStatement.setString(2, userID);
			preparedStatement.setString(3, pollID);
			preparedStatement.executeUpdate();
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
