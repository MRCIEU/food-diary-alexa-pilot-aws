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


//import com.fasterxml.jackson.databind.ObjectMapper;

public class IntakeEvent implements Serializable {
	
	private static final long serialversionUID = 1237L;
	protected List<IntakeElement> intakeElements;
	protected LocalDate intakeDate;
	protected LocalTime intakeTime;
	protected String userID;
	protected String deviceID;
	protected String intakeTimeOfDay;
	protected String breadcrumbs = "";
		
	public IntakeEvent() {
		intakeElements = new ArrayList<IntakeElement>();
	}
	
	public IntakeEvent(String userID, String deviceID) {
		intakeElements = new ArrayList<IntakeElement>();
		
		this.userID = userID;
		this.deviceID = deviceID;
	}
	
	public static IntakeEvent bytesToEvent(String byteString) {
	
		if (byteString==null) {
			return new IntakeEvent();
		}
		else {
			byte[] bytes = Base64.getDecoder().decode(byteString);
			IntakeEvent ie = SerializationUtils.deserialize(bytes);
			return ie;
		}
		
	}
	
	public String getByteString() {
		byte [] bytes = SerializationUtils.serialize(this);
		String s = Base64.getEncoder().encodeToString(bytes);
		return s;
	}
	
	public boolean isEmpty() {
	
		if (hasDate()==true || hasTime()==true || intakeElements.size()>0) {
			return false;
		}
		else
			return true;
	}
	
	public void setIntakeTimeOfDay(String intakeTimeOfDay) {
		this.intakeTimeOfDay = intakeTimeOfDay;
	}
	
	public String getIntakeTimeOfDay() {
		return intakeTimeOfDay;
	}
	
	public boolean hasIntakeTimeOfDay() {
		if (getIntakeTimeOfDay()!=null)
			return true;
		else
			return false;
	}
	
	
	/*
	public static IntakeEvent docToEvent(Document doc) {
		
		IntakeEvent event = new IntakeEvent();
		
		if (doc!=null) {
		
			String tStr = doc.getString("intake_time");
			if (tStr!=null)
				event.setIntakeTime(LocalTime.parse(tStr));

			String dStr = doc.getString("intake_date");
			if (dStr!=null)
				event.setIntakeDate(LocalDate.parse(dStr));
			
			String userID = doc.getString("userID");
			if (userID!=null)
				event.setUserID(userID);
		
			ArrayList<LinkedHashMap> elements = (ArrayList<LinkedHashMap>)map.get("intakeElements");
		
			Iterator<LinkedHashMap> iter = elements.iterator();
			while (iter.hasNext()) {
				LinkedHashMap elementMap = iter.next();
				
				IntakeElement e = new IntakeElement();
				
				if (map.get("intakeTime")!=null)
				e.setQuantity(Integer.parseInt((String)map.get("intakeTime")));
				e.setQuantityType((String)map.get("quantityType"));
				e.setDescription((String)map.get("description"));
				
				event.addElement(e);
			}
		
		
		}
		
		return event;
	}*/
	
	public boolean hasTime() {
		if (getIntakeTime()!=null)
			return true;
		else
			return false;
	}
	
	public boolean hasDate() {
		if (getIntakeDate()!=null)
			return true;
		else
			return false;
	}	
	
	public boolean hasUserID() {
		if (getUserID()!=null)
			return true;
		else
			return false;
	}	
	
	public List<IntakeElement> getIntakeElements() {
		return intakeElements;
	}
	
	public void setIntakeElements(List<IntakeElement> intakeElements) {
		this.intakeElements = intakeElements;
	}
	
	
	public LocalDate getIntakeDate() {
		return intakeDate;
	}
	
	public void setIntakeDate(LocalDate intakeDate) {
		this.intakeDate = intakeDate;
	}
	
	public LocalTime getIntakeTime() {
		return intakeTime;
	}
	
	public void setIntakeTime(LocalTime intakeTime) {
		this.intakeTime = intakeTime;
	}
	
	public LocalDateTime getIntakeDateTime() {
		if (intakeTime!=null && intakeDate!=null) {
			return LocalDateTime.of(intakeDate, intakeTime);
		}
		else
			return null;
	}
	
	public String getUserID() {
		return userID;
	}
	
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	public String getDeviceID() {
		return deviceID;
	}
	
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	public String getBreadcrumbs() {
		return breadcrumbs;
	}
	
	public void setBreadcrumbs(String breadcrumbs) {
		this.breadcrumbs = breadcrumbs;
	}

	public void addBreadcrumb(String breadcrumb) {
		this.breadcrumbs += " "+breadcrumb;
	}
	
	public void addElement(IntakeElement element) {
		intakeElements.add(element);
	}
	
	//public boolean lastElementHasQuantity() {
	//	return intakeElements.get(intakeElements.size()-1).hasQuantity();
	//}
	
	public String toString() {

		String sessionContents = "";
		
		sessionContents += "time_of_day[" + getIntakeTimeOfDay() + "] ";
		
		String userID = getUserID();
		if (userID != null) {
			sessionContents += "user_id[" + userID + "] ";
		}

		LocalDate date = getIntakeDate();
		if (date != null) {
			sessionContents += "date[" + date + "] ";
		}
		
		LocalTime time = getIntakeTime();
		if (time != null) {
			sessionContents += "time[" + time + "] ";
		}
		
		Iterator<IntakeElement> iter = intakeElements.iterator();
		
		sessionContents += "num_elements[" + intakeElements.size() + "]:: ";
		int count=0;
		while (iter.hasNext()) {
			IntakeElement element = iter.next();
			sessionContents +=  "element_" + count++ + ":" + element.toString() + ";";
		}
		
		return sessionContents;
		
	}
	
	public String toSpokenString() {

		String sessionContents = "";
		
		if (isEmpty()) {
			return "The event is empty. ";
		}
		else {
		
			String userID = getUserID();
			if (userID != null) {
				sessionContents += "Your user id is  " + userID + ".";
			}

			LocalDate date = getIntakeDate();
			if (date != null) {
				sessionContents += "On " + date + ".";
			}
		
			LocalTime time = getIntakeTime();
			if (time != null) {
				sessionContents += "At " + time;
			}
		
			Iterator<IntakeElement> iter = intakeElements.iterator();
		
			while (iter.hasNext()) {
			
				IntakeElement element = iter.next();
				sessionContents += element.toSpokenString();
			
			}
		}
		
		return sessionContents;
		
	}
	
}

