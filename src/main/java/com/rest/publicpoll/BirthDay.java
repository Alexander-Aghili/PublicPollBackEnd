package com.rest.publicpoll;

import java.sql.Date;

/*
 * Copyright © 2021 Alexander Aghili - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexander Aghili alexander.w.aghili@gmail.com, June 2021
 */

public class BirthDay 
{
	private int day;
	private int month;
	private int year;
	
	public BirthDay(int day, int month, int year) {
		this.day = day;
		this.month = month;
		this.year = year;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}
	
	//Something about local ignorance for just putting a year, month, and day or something idrc but its here.
	public Date toSQLDate() {
		String dateString = String.format("%d-%02d-%02d", year, month, day);
		return Date.valueOf(dateString);
	}
	
}
