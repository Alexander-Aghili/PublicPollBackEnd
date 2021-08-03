package com.rest.publicpoll;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

	public Log(String message, boolean isVerbose) {
		
		String logFile;
		
		if (isVerbose == true) {
			logFile = "verbose.log";
		} else {
			logFile = "errors.log";
		}
		
		try {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
			LocalDateTime now = LocalDateTime.now(); 
			BufferedWriter bw = new BufferedWriter(new FileWriter(logFile));
			bw.append(message + " " + dtf.format(now));
			bw.close();
			
		} catch (IOException e) {
			System.out.println("Critical Log Writing Fail.");
			e.printStackTrace();
		}
		
		
	}
	
}
