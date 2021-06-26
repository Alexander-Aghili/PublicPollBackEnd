package com.rest.publicpoll;

import java.sql.Date;

import org.json.JSONObject;

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
	
	public BirthDay(int year, int month, int day) {
		this.day = day;
		this.month = month;
		this.year = year;
	}
	
	public BirthDay(Date date) {
		String dateString = date.toString();
		int firstDash = dateString.indexOf("-");
		int secondDash = dateString.lastIndexOf("-");
		year = Integer.parseInt(dateString.substring(0, firstDash));
		month = Integer.parseInt(dateString.substring(firstDash + 1, secondDash));
		day = Integer.parseInt(dateString.substring(secondDash + 1));
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
	
	public String toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("year", year);
		jo.put("month", month);
		jo.put("day", day);
		return jo.toString();
	}
	
	@Override
	public String toString() {
		return year+"-"+month+"-"+day;
	}
	
}
