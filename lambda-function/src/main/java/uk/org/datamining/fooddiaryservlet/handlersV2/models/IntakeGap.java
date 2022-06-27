package uk.org.datamining.fooddiaryservlet.handlersV2.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.io.*;
import org.apache.commons.lang3.SerializationUtils;
import java.util.Base64;


public class IntakeGap implements Serializable {
	
	private static final long serialversionUID = 1236L;
	protected LocalDateTime startDate;
	protected LocalDateTime endDate;
	protected Long userID;
	
	public IntakeGap() {
	}
	
	public IntakeGap(Long userID) {		
		this.userID = userID;
	}
	
	public static IntakeGap bytesToIntakeGap(String byteString) {
	
		if (byteString==null) {
			return new IntakeGap();
		}
		else {
			byte[] bytes = Base64.getDecoder().decode(byteString);
			IntakeGap ie = SerializationUtils.deserialize(bytes);
			return ie;
		}
		
	}
	
	public String getByteString() {
		byte [] bytes = SerializationUtils.serialize(this);
		String s = Base64.getEncoder().encodeToString(bytes);
		return s;
	}
	
	public Long getUserID() {
		return userID;
	}
	
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	
	public LocalDateTime getStartDate() {
		return startDate;
	}
	
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}
	
	public LocalDateTime getEndDate() {
		return endDate;
	}
	
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	
	public String toString() {

		String sessionContents = "";
		
		Long userID = getUserID();
		if (userID != null) {
			sessionContents += "User id: " + userID + "; ";
		}

		LocalDateTime start = getStartDate();
		if (start != null) {
			sessionContents += "Start: " + start + ";";
		}
		
		LocalDateTime end = getEndDate();
		if (end != null) {
			sessionContents += "End: " + end + ";";
		}
		
		return sessionContents;
		
	}
	
	
}

