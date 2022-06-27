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
import com.amazon.ask.attributes.AttributesManager;

import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionContentChecker {

	static Logger logger = LoggerFactory.getLogger(SessionContentChecker.class);

	// success and failure text that prefix the main body of an alexa reply depending on if the users last input was ok or not
	// we cycle through these so the conversation is more natural
	static String [] confusedPhrases = {"sorry, I'm a bit confused. ", "hmm, I'm having trouble understanding. ", "sorry, I'm not sure what you mean. "};
	static String [] successPhrases = {"great, ", "thanks, ", "ok great, ", "ok, ", "ok thanks, ", "that's great, "};
	static int confusedNum = 0;
	static int successNum = 0;

	public static String nextPart(IntakeEvent event) {

		logger.info("XXXXXXXXXXXXXXXXXXXXX SessionContentChecker.nextPart");
		logger.info("XXXXXXXXXXXXXXXXXXXXX " + event.toString());
		
		// we ask for an intake event details in the order: participant ID, date/time, intake elements
		
		/*if (event.hasUserID() == false) {
			return ConsumptionPart.USER_ID.name();
		}
		else */
		if (event.hasDate() == false && event.hasTime() == false) {
			return ConsumptionPart.CONSUMPTION_DATE_TIME.name();
		}
		else if (event.hasDate() == false) {
			return ConsumptionPart.CONSUMPTION_DATE.name();
		}
		else if (event.hasTime() == false) {
			return ConsumptionPart.CONSUMPTION_TIME.name();
		}
		else if (event.getIntakeElements().size()==0){
			return ConsumptionPart.INTAKE_ELEMENT.name();
		}
		else 
			return ConsumptionPart.SUBMIT.name();

	}

	public static String completeEvent(IntakeEvent event) {

		DatabaseInputOutputTool.addAlexaIntakeEvent(event);

		String responseText = event.toSpokenString() + " Thanks we have all the information.";
		return responseText;

	}
	
	public static Optional<Response> getResponse(HandlerInput input, Map<String,Object> attributes, boolean success) {
		return getResponse(input, attributes, success, null);
	}

	public static Optional<Response> getResponse(HandlerInput input, Map<String,Object> attributes, boolean success, String prefixText) {

		AttributesManager attributesManager = input.getAttributesManager();
		IntakeEvent event = IntakeEvent.bytesToEvent((String)attributes.get("current_event"));
		String part = SessionContentChecker.nextPart(event);
		
		logger.info("XXXXX event " + event.toString());
		logger.info("XXXXX part " + part);
		
		String breadcrumbs = event.getBreadcrumbs();
		
		String speechText = null;
		String repromptText = null;
		
		// intake element issue - our logic doesn't think that either a quantity of food/drink was supplied
		//String currentFail = (String)attributes.get("currentFail");
		
		String currentQ = (String)attributes.get("currentQuestion");
		
				
		if (currentQ!=null && !currentQ.equals("")) {
		
			if (currentQ.equals("new_event") || currentQ.equals("what_in_gap")) {
			
				speechText = "do you have other events to submit, for other times?";
				repromptText = "do you have other events to submit?";
			
				//breadcrumbs += " QUESTION_GAP";
				event.addBreadcrumb("QUESTION_NEW_EVENT");
			}
			else {
				logger.info("XXXXX SHOULD NOT GET HERE " + currentQ);
			}
		}
		/*		
		else if (currentFail!=null && currentFail.equals("PARTICIPANT_ID")) {
		
			event.addBreadcrumb("FAIL_PID");
			speechText = "hmm, I couldn't find that one, please can you try again?";
			repromptText = "please let me know your participant identifier.";
			
			attributes.put("currentFail", "");
		}
		else if (part.equals(ConsumptionPart.USER_ID.name())) {
			event.addBreadcrumb("PART_PID");
			speechText = "what is your participant identifier?";
			repromptText = "please let me know your participant identifier.";
		
		} */ 
		else if (part.equals(ConsumptionPart.CONSUMPTION_DATE_TIME.name())) {
			event.addBreadcrumb("PART_DATETIME");
			if (success == true) {
				speechText = "when?";
			}
			else {
				speechText = "when did you eat or drink something?";
			}
			repromptText = "please let me know when you ate or drank something.";
			
		} else if (part.equals(ConsumptionPart.CONSUMPTION_DATE.name())) {
			event.addBreadcrumb("PART_DATE");
			speechText = "was it today, yesterday or another day?";
			repromptText = "please tell me if it was today, yesterday or another day.";
			
		}
		else if (part.equals(ConsumptionPart.CONSUMPTION_TIME.name())) {
			event.addBreadcrumb("PART_TIME");
			speechText = "what time?";
			repromptText = "please tell me the time you had this";
			
		}
		else if (part.equals("INTAKE_ELEMENT")) {
			event.addBreadcrumb("PART_FOODDRINK");
			if (success == true) {
				speechText = "please add items";
			}
			else {
				speechText = "please add what you ate and drank, for example \"add item, a packet of salt and vinegar crisps";
			}
			repromptText = "tell me what you had to eat or drink at that time";
			
		}
		else if (part.equals(ConsumptionPart.SUBMIT.name())) {
			event.addBreadcrumb("PART_SUBMIT");
			// check if they want to add any other food or drink
			
			if (success == true) {
				speechText = "Anything else?";
			}
			else {
				speechText = "please add any other items you had at this time or submit this event.";
			}
			
			repromptText = "Do you have other items that you ate or drank at this time?";
			
		}

		
		
		if (prefixText!=null) {
			speechText = prefixText + speechText;
		}
		else if (success == true) {
			
			// cycle through success phrases for variation
			speechText = successPhrases[successNum] + speechText;
			successNum = (successNum==successPhrases.length-1)?successNum=0:++successNum;
						
		}
		else if (success == false) {
			
			// cycle through failure phrases for variation		
			speechText = confusedPhrases[confusedNum] + speechText;
			confusedNum = (confusedNum==confusedPhrases.length-1)?confusedNum=0:++confusedNum;
			
		}

		// include event info for testing
		//speechText =  speechText + " | ... | " + event.toString();
	
		// update event with new breadcrumbs
		attributes.put("current_event", event.getByteString());
		attributesManager.setSessionAttributes(attributes);
		
		return input.getResponseBuilder()
			.withSpeech(speechText)
			.withReprompt(repromptText)
			.build();
	}


	public static void clearSession(AttributesManager attributesManager) {

		Map<String,Object> attributes = attributesManager.getSessionAttributes();
		
		IntakeEvent eventOld = IntakeEvent.bytesToEvent((String)attributes.get("current_event"));
		
		IntakeEvent event = new IntakeEvent(eventOld.getUserID(), eventOld.getDeviceID());
		
		attributes.put("current_event", event.getByteString());
		attributesManager.setSessionAttributes(attributes);
		
	}
	
	
    
    
	public static boolean attributeExists(Map<String,Object> attributes, String attributeName) {
		
		// either a string or a CONSUMPTION_TYPE
        Object att = attributes.get(attributeName);
		if (att == null || att.toString().trim().equals("")) {
			return false;
		}
		else {
			return true;
		}

	}

}


