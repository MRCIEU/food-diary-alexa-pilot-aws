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

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.amazon.ask.attributes.AttributesManager;

import com.amazon.ask.model.Slot;

import java.util.Optional;
import java.util.Map;
import java.util.List;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.temporal.ChronoUnit;

import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;

import static com.amazon.ask.request.Predicates.intentName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumptionDateAndTimeIntentHandler implements RequestHandler {

	private static Logger logger = LoggerFactory.getLogger(ConsumptionDateAndTimeIntentHandler.class);

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ConsumptionDateAndTimeIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		logger.info("XXXXXXXXXXXXXXXXXXXXX ConsumptionDateAndTimeIntentHandler.handle");

		// map input to right consumption type
		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		AttributesManager attributesManager = input.getAttributesManager();
		Map<String,Object> attributes = attributesManager.getSessionAttributes();

		IntakeEvent event = IntakeEvent.bytesToEvent((String)attributes.get("current_event"));
		
		logger.info("XXXXX " + event.toString());
		
		boolean response = true;
		// meant to be entering a date and or time
		if (SessionContentChecker.nextPart(event).equals("CONSUMPTION_DATE_TIME") || SessionContentChecker.nextPart(event).equals("CONSUMPTION_DATE") || SessionContentChecker.nextPart(event).equals("CONSUMPTION_TIME")) {

			response = ConsumptionDateAndTimeIntentHandler.setDateTime(input, event);
			//attributes.put("currentFail", "");

		}
		else {
			logger.info("XXXXX WRONG INTENT");
			
			event.addBreadcrumb("DEFAULT_DATETIMEINTENT");
			attributes.put("current_event", event.getByteString());
			attributesManager.setSessionAttributes(attributes);
		
			response = false;
		}
		
		return SessionContentChecker.getResponse(input, attributes, response);

	}

	public static boolean setDateTime(HandlerInput input, IntakeEvent event) {
	
		AttributesManager attributesManager = input.getAttributesManager();
		Map<String,Object> attributes = attributesManager.getSessionAttributes();
		
		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		
		Optional<String> intakeDatex = requestHelper.getSlotValue("intake_date");
		Optional<String> intakeTimex = requestHelper.getSlotValue("intake_time");
		Optional<String> numMinsAgo = requestHelper.getSlotValue("num_mins");
		Optional<String> numHoursAgo = requestHelper.getSlotValue("num_hours");
		Optional<String> numDaysAgo = requestHelper.getSlotValue("num_days");
		Optional<String> relativeDay = requestHelper.getSlotValue("relative_day");
		Optional<String> fractionHour = requestHelper.getSlotValue("fraction_hour");
		
		
		// slot to recognise intake that just happened
		Optional<String> justTime = requestHelper.getSlotValue("just_time");
		
		//logger.info("XXXXXXXXXXXXXXXXXXXXX intakeDatex " + intakeDatex);
		//logger.info("XXXXXXXXXXXXXXXXXXXXX intakeTimex " + intakeTimex);
		//logger.info("XXXXXXXXXXXXXXXXXXXXX numMinsAgo " + numMinsAgo);
		//logger.info("XXXXXXXXXXXXXXXXXXXXX numHoursAgo " + numHoursAgo);
		//logger.info("XXXXXXXXXXXXXXXXXXXXX numDaysAgo " + numDaysAgo);
		//logger.info("XXXXXXXXXXXXXXXXXXXXX relativeDay " + relativeDay);
		//logger.info("XXXXXXXXXXXXXXXXXXXXX fractionHour " + fractionHour);
		//logger.info("XXXXXXXXXXXXXXXXXXXXX justTime " + justTime);
			
		
		LocalDate intakeDate = null;
		LocalTime intakeTime = null;
		
		boolean success = false;
		
		if (intakeDatex.isPresent()) {
			// if date exists try and set the time also from intake_time
			
			intakeDate = LocalDate.parse(intakeDatex.get());
			event.setIntakeDate(intakeDate);
						
			// set time from standard time input
			event = ConsumptionDateAndTimeIntentHandler.setTime(event, intakeTimex);

			success = true;
		}
		else if (numDaysAgo.isPresent()) {
			// else if num_days exists try and set the time also from intake_time
		
			intakeDate = LocalDate.now();
			intakeDate = intakeDate.minusDays(Integer.parseInt(numDaysAgo.get()));
			event.setIntakeDate(intakeDate);

			// set time from standard time input
			event = setTime(event, intakeTimex);

			success = true;			
		}
		else if (numHoursAgo.isPresent()) {
			// else if num_hours exists set the date and time as num_hours before the current date/time
			
			LocalDateTime timenow = LocalDateTime.now();
			LocalDateTime intakeDateTime = timenow.minusHours(Integer.parseInt(numHoursAgo.get()));
			
			intakeDate = intakeDateTime.toLocalDate();		
			event.setIntakeDate(intakeDate);
			
			// set time as num hours before current time
			try {
				intakeTime = intakeDateTime.toLocalTime();
				event.setIntakeTime(intakeTime);
			}
			catch (java.time.format.DateTimeParseException e) {
					String timeString = intakeTimex.get();
					event.setIntakeTimeOfDay(timeString);
			}
			success = true;
		}
		else if (fractionHour.isPresent()) {
			
			int numMinsAgoX = 0;
			String fractionStr = fractionHour.get();
			if (fractionStr.equals("half")) {
				numMinsAgoX = 30;
			}
			else if (fractionStr.equals("quarter")) {
				numMinsAgoX = 15;			
			}
			
			// TODO might need to deal with non half and quarter fractionHour values if they occur
			LocalDateTime timenow = LocalDateTime.now();
			LocalDateTime intakeDateTime = timenow.minusMinutes(numMinsAgoX);
		
			intakeDate = intakeDateTime.toLocalDate();
			event.setIntakeDate(intakeDate);
			
			intakeTime = intakeDateTime.toLocalTime();
			event.setIntakeTime(intakeTime);
			success = true;
		}
		else if (numMinsAgo.isPresent()) {
			// else if num_mins exists set the date and time as num_mins before the current date/time
			
			LocalDateTime timenow = LocalDateTime.now();
			LocalDateTime intakeDateTime = timenow.minusMinutes(Integer.parseInt(numMinsAgo.get()));
			intakeDate = intakeDateTime.toLocalDate();
			event.setIntakeDate(intakeDate);
			
			// set time as num minutes before current time
			try {
				intakeTime = intakeDateTime.toLocalTime();
				event.setIntakeTime(intakeTime);
			}
			catch (java.time.format.DateTimeParseException e) {
					String timeString = intakeTimex.get();
					event.setIntakeTimeOfDay(timeString);
			}
			success = true;
		}
		else if (relativeDay.isPresent()) {
		
			String relativeDayInfo = relativeDay.get();

			if (relativeDayInfo.equals("the day before yesterday")) {
				intakeDate = LocalDate.now();
				intakeDate = intakeDate.minusDays(2);	
				event.setIntakeDate(intakeDate);
				
				// set time from standard time input
				event = ConsumptionDateAndTimeIntentHandler.setTime(event, intakeTimex);
				
				success = true;
			} 
			else if (relativeDayInfo.equals("yesterday afternoon") || relativeDayInfo.equals("last night")) {
			
				intakeDate = LocalDate.now();
				intakeDate = intakeDate.minusDays(1);
				event.setIntakeDate(intakeDate);
				
				event.setIntakeTimeOfDay(relativeDayInfo);
				
				// set the time as the pm version e.g. 10 oclock is 10pm
				// this is needed because if someone says 'ten o'clock' alexa assumes 24 hour wording so 10 am
				if (intakeTimex.isPresent()) {
					
					try {
						intakeTime = LocalTime.parse(intakeTimex.get());
						
						if (intakeTime.getHour()<12) {
							// add 12 hours to turn e.g. 10am to 10pm
							intakeTime = intakeTime.plus(12, ChronoUnit.HOURS);
						}
						
						event.setIntakeTime(intakeTime);
					}
					catch (java.time.format.DateTimeParseException e) {
						String timeString = intakeTimex.get();
						event.setIntakeTimeOfDay(timeString);
					}
				}
				success = true;
			}
			else if (relativeDayInfo.equals("yesterday morning")) {
			
				intakeDate = LocalDate.now();
				intakeDate = intakeDate.minusDays(1);	
				event.setIntakeDate(intakeDate);
				event.setIntakeTimeOfDay(relativeDayInfo);
				
				// set time from standard time input
				event = ConsumptionDateAndTimeIntentHandler.setTime(event, intakeTimex);
				success = true;
			}
			else if (relativeDayInfo.equals("this morning")) {
				
				intakeDate = LocalDate.now();
				event.setIntakeDate(intakeDate);
				event.setIntakeTimeOfDay(relativeDayInfo);
				
				// set time from standard time input
				event = ConsumptionDateAndTimeIntentHandler.setTime(event, intakeTimex);
				success = true;
			}
			else if (relativeDayInfo.equals("this afternoon") || relativeDayInfo.equals("tonight")) {
				
				intakeDate = LocalDate.now();
				event.setIntakeTimeOfDay(relativeDayInfo);
				event.setIntakeDate(intakeDate);
				
				// set the time as the pm version e.g. 10 oclock is 10pm
				if (intakeTimex.isPresent()) {
					
					try {
						intakeTime = LocalTime.parse(intakeTimex.get());
						
						if (intakeTime.getHour()<12) {
							// add 12 hours to turn e.g. 10am to 10pm
							intakeTime = intakeTime.plus(12, ChronoUnit.HOURS);
						}
						
						event.setIntakeTime(intakeTime);
					}
					catch (java.time.format.DateTimeParseException e) {
						String timeString = intakeTimex.get();
						event.setIntakeTimeOfDay(timeString);
					}
				}
				success = true;
			}
		}
		else if (intakeTimex.isPresent()) {
			// else if time exists then set the time and leave the date unset
			
			// set time from standard time input
			event = ConsumptionDateAndTimeIntentHandler.setTime(event, intakeTimex);
			
			// if they already entered time_of_day and it's the afternoon or last night then make sure the time is in the afternoon
			if (event.getIntakeTimeOfDay()!=null && (event.getIntakeTimeOfDay().contains("night") || event.getIntakeTimeOfDay().contains("afternoon")) && event.getIntakeTime().getHour()<12) {
				// add 12 hours to turn e.g. 10am to 10pm
				event.setIntakeTime(event.getIntakeTime().plus(12, ChronoUnit.HOURS));
			}
			
			// assume today if date not given and it's not already been set
			//if (event.getIntakeDate() == null) {
			//	LocalDate nowDate = LocalDate.now();
			//	event.setIntakeDate(nowDate.toLocalDate());
			//}
			success = true;
		}
		else if (justTime.isPresent()) {
		
			// event just happened so set date/time as now
			LocalDateTime timenow = LocalDateTime.now();
			event.setIntakeTime(timenow.toLocalTime());
			event.setIntakeDate(timenow.toLocalDate());
			success = true;
		}
		
		attributes.put("current_event", event.getByteString());
		attributesManager.setSessionAttributes(attributes);
		
		
		return success;
	}
	
	public static IntakeEvent setTime(IntakeEvent event, Optional<String> intakeTimex) {
		if (intakeTimex.isPresent()) {
			
				try {
					LocalTime intakeTime = LocalTime.parse(intakeTimex.get());
					event.setIntakeTime(intakeTime);
				}
				catch (java.time.format.DateTimeParseException e) {
					String timeString = intakeTimex.get();
					event.setIntakeTimeOfDay(timeString);
				}
			}
		return event;
	
	}

}
