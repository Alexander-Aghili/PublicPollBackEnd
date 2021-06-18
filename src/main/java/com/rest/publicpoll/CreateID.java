package com.rest.publicpoll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class CreateID {
	
	//Creates a valid user ID and returns it
	public static String createID(int length, PreparedStatement preparedStatement, Connection connect, String table, String parameter) throws Exception {
		boolean isUniqueID = false;
		String id = "";
		
		while (!isUniqueID) {
			String tempID = generateRandomID(length);
			preparedStatement = connect.prepareStatement("SELECT * FROM " + table + " WHERE ? = ?");
			preparedStatement.setString(1, parameter);
			preparedStatement.setString(2, tempID);
			ResultSet results  = preparedStatement.executeQuery();
			if (!results.isBeforeFirst()) {
				id = tempID;
				isUniqueID = true;
			}
		}
		return id;
		
	}
	
	//Random ID generator
	private static String generateRandomID(int length) throws Exception {
		char[] characters = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 
				'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
		String id = "";
		
		for (int i = 0; i < length; i++) {
			Random rand = new Random();
			int num = rand.nextInt(61);
			id += String.valueOf(characters[num]);
		}
		return id;
	}
}
