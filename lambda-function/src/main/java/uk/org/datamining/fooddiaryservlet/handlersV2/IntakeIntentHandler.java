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

import java.util.Optional;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;


import static com.amazon.ask.request.Predicates.intentName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;
//import uk.org.datamining.fooddiaryservlet.handlersV2.predictors.*;

public class IntakeIntentHandler implements RequestHandler {

	static Logger logger = LoggerFactory.getLogger(IntakeIntentHandler.class);

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("IntakeIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		logger.info("XXXXXXXXXXXXXXXXXXXXX IntakeIntentHandler.handle");

		// map input to right consumption type
		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);


		Optional<String> intake = requestHelper.getSlotValue("intake");
		
		logger.info("XXXXX " + intake.get());
		
		
		AttributesManager attributesManager = input.getAttributesManager();
		Map<String,Object> attributes = attributesManager.getSessionAttributes();	
		
		// current event
		IntakeEvent event = IntakeEvent.bytesToEvent((String)attributes.get("current_event"));
		
		if (SessionContentChecker.nextPart(event).equals("INTAKE_ELEMENT") || SessionContentChecker.nextPart(event).equals("SUBMIT")) {
		
			
			// add item to intake event
			addFoodDrink(input, event, intake);
			
			attributesManager.setSessionAttributes(attributes);
		
			return SessionContentChecker.getResponse(input, attributes, true);
		}
		else {
			event.addBreadcrumb("DEFAULT_INTAKEINTENT");
			attributes.put("current_event", event.getByteString());
			attributesManager.setSessionAttributes(attributes);
			
			return SessionContentChecker.getResponse(input, attributes, false);
		}
		
	}
	
	public static void addFoodDrink(HandlerInput input, IntakeEvent event, Optional<String> intake) {
	
		//String speechText = "food_intake: " + intake.get();
		//logger.info("XXXXX input " + speechText);
		
		if (intake.isPresent()) {
			AttributesManager attributesManager = input.getAttributesManager();
			Map<String,Object> attributes = attributesManager.getSessionAttributes();
		
			IntakeElement element = new IntakeElement();
			element.setDescription(intake.get());
			event.addElement(element);
		
			attributes.put("current_event", event.getByteString());

			attributesManager.setSessionAttributes(attributes);
		}
	
	}
	

}
