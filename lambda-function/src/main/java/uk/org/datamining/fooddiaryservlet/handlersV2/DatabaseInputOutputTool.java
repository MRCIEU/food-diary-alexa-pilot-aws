// The MIT License (MIT)
//
// Copyright (c) 2019 Louise AC Millard, MRC Integrative Epidemiology Unit, University of Bristol
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the "Software"), to deal in the Software without restriction, including without
// limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
// the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions
// of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
// TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
// CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.


package uk.org.datamining.fooddiaryservlet.handlersV2;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;

import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;


public class DatabaseInputOutputTool {
	
	static Logger logger = LoggerFactory.getLogger(DatabaseInputOutputTool.class);

	static String url = "jdbc:mysql://"+System.getenv().get("RDS_HOST_NAME")+":3306/fooddiary";
	static String username = System.getenv().get("RDS_FD_USERNAME");
	static String pwd = System.getenv().get("RDS_FD_PWD");
      
	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	AmazonRDS awsRDS = AmazonRDSClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
	
	public static void dbTest() {
	
		
//		String pwd = StringParameter.valueForSecureStringParameter(this, "/alexaFoodDiary/value1",1); 
		//String pwd = getParameterFromSSMByName("/alexaFoodDiary/value1");
		
		
		String hostname = System.getenv().get("RDS_HOST_NAME");
		logger.info("XXXXXXXXX HOSTNAME " + hostname);
	
		String url = "jdbc:mysql://"+hostname+":3306/fooddiary";
      	String username = System.getenv().get("RDS_FD_USERNAME");
      	String pwd = System.getenv().get("RDS_FD_PWD");
      
		String currentTime = "unavailable";
      
      try {
		logger.info("conn ...");
      	Connection conn = DriverManager.getConnection(url, username, pwd);
      	Statement stmt = conn.createStatement();
		logger.info("exec ...");
		
		
		
		ResultSet resultSet = stmt.executeQuery("SELECT NOW()");
      	if (resultSet.next()) {
      	  currentTime = resultSet.getObject(1).toString();
      	}
      	logger.info("current time: " + currentTime); 

		logger.info("exec end ");      

      } catch (Exception e) {
		e.printStackTrace();
    	logger.info("Caught exception: " + e.getMessage());
      }
  
      
    }
    
    public static boolean addAlexaIntakeEvent(IntakeEvent event) {

		return addAlexaIntakeEvent(event, false);
	}
    
	public static boolean addAlexaIntakeEvent(IntakeEvent event, boolean partial) {

      try {
		logger.info("conn ...");
      	Connection conn = DriverManager.getConnection(url, username, pwd);
      	
      	
      	String sqlInsert = "INSERT INTO intake_event_alexa (user_id, device_id, creation_timestamp, breadcrumbs, intake_time, intake_time_of_day, intake_date, partial) values (?,?,?,?,?,?,?,?)";
      	PreparedStatement statement = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
		
		logger.info("exec ...");
		
		LocalDateTime ldt = LocalDateTime.now();
		String datetimenow = ldt.format(formatter);
		logger.info("datetimenow: " + datetimenow);
		
		statement.setString(1, event.getUserID());
		statement.setString(2, event.getDeviceID());
		statement.setString(3, datetimenow);
		statement.setString(4, event.getBreadcrumbs());
		statement.setTime(5, Time.valueOf(event.getIntakeTime()));
		statement.setString(6, event.getIntakeTimeOfDay());
		statement.setDate(7, Date.valueOf(event.getIntakeDate()));
		statement.setBoolean(8, partial);
		
		
		int affectedRows = statement.executeUpdate();
		ResultSet generatedKeys = statement.getGeneratedKeys();
		//logger.info("get key start ...");
		
		long key = -1;
		if (generatedKeys.next()) {
                key = generatedKeys.getLong(1);
                
        }
        //logger.info("key: " + key);
		//logger.info("get key end ...");
		
		logger.info("intake event added ...");

		////
		//// insert each element in this event

		conn.close();
		Connection conn2 = DriverManager.getConnection(url, username, pwd);

		Iterator<IntakeElement> elementsIter = event.getIntakeElements().iterator();
		
		while (elementsIter.hasNext()) {
		
			IntakeElement element = elementsIter.next();
			
			String sqlInsertElement = "INSERT INTO intake_element_alexa (event_id, description, creation_timestamp) values (?,?,?)";
			PreparedStatement statement2 = conn2.prepareStatement(sqlInsertElement, Statement.RETURN_GENERATED_KEYS);

			//logger.info(""+key);
			//logger.info(""+element.getDescription());
			//logger.info(""+datetimenow);
			
			statement2.setLong(1, key);	
			statement2.setString(2, element.getDescription());
			statement2.setString(3, datetimenow);

			//logger.info("adding intake element ...");

			affectedRows = statement2.executeUpdate();
			
			logger.info("intake element added ...");

			}
		
		
			conn2.close();
		
		
		} catch (Exception e) {
			e.printStackTrace();
    		logger.info("Caught exception: " + e.getMessage());
      	}

		return true;
    }
    

}
