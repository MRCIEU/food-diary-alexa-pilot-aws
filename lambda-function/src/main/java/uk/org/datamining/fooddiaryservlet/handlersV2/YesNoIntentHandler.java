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

import com.amazon.ask.model.slu.entityresolution.Resolutions;
import com.amazon.ask.model.slu.entityresolution.Resolution;
import com.amazon.ask.model.slu.entityresolution.StatusCode;
import com.amazon.ask.model.Slot;

import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.Arrays;


import static com.amazon.ask.request.Predicates.intentName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;

public class YesNoIntentHandler implements RequestHandler {

	static Logger logger = LoggerFactory.getLogger(YesNoIntentHandler.class);

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("YesNoIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		logger.info("XXXXXXXXXXXXXXXXXXXXX YesNoIntentHandler.handle");

		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		AttributesManager attributesManager = input.getAttributesManager();
		Map<String,Object> attributes = attributesManager.getSessionAttributes();
	
		IntakeEvent event = IntakeEvent.bytesToEvent((String)attributes.get("current_event"));	
		
		// add breadcrumb to event	
		event.addBreadcrumb("INTENT_YESNO");
		attributes.put("current_event", event.getByteString());
		
		
		Optional<String> yesOrNo = requestHelper.getSlotValue("yes_or_no");
		
		
		// correct use of yesNoIntent is either for
		// filling in a gap (NOT CURRENTLY USED) or adding another event after completing one, or after adding an item
		
		String currentQ = (String)attributes.get("currentQuestion");

		logger.info("XXXXX YesNoIntentHandler.handle " + currentQ);
		logger.info("XXXXX YesNoIntentHandler.handle " + yesOrNo.isPresent());
		
		Boolean isYes = YesNoIntentHandler.isYes(yesOrNo);
		
		if (isYes!=null) {

			logger.info("XXXXX YesNoIntentHandler.handle " + yesOrNo.get());
			
			String part = SessionContentChecker.nextPart(event);
			boolean doAnythingElseQ = part.equals(ConsumptionPart.SUBMIT.name());
			boolean doNewEventQ = currentQ!=null && currentQ.equals("new_event");
			boolean doWhatInGapQ = currentQ!=null && currentQ.equals("what_in_gap");
		  		
		  	String prefix="";
		  		
			if (doWhatInGapQ) {
				// current question asks if they had food/drink between last two intake events

				// THIS GAP LOGIC IS UNUSED
				
				// store gap even if then then fill it in, so we know what missingness we have sorted out
//				String bytesGap = (String) attributes.get("intake_gap");
//				IntakeGap gap = IntakeGap.bytesToIntakeGap(bytesGap);
//				DatabaseInputOutputTool.addAlexaGapInfo(gap, yesOrNo.get());
//				attributes.put("intake_gap", null);
				
//				attributesManager.setSessionAttributes(attributes);			
//				return SessionContentChecker.getResponse(input, attributes, true);
		
			}
			else if (doNewEventQ) {
				
				// if user says yes they want to add a new event, if they say no then they have finished adding new events
				if (isYes==true) {
			
					attributes.put("currentQuestion", "");
					attributesManager.setSessionAttributes(attributes);
			
					// continue to next event
					return SessionContentChecker.getResponse(input, attributes, true, "OK, ");
				
				}
				else if (isYes==false) {
				
					event.addBreadcrumb("YESNO_END_SESSION");
					attributes.put("current_event", event.getByteString());
					attributesManager.setSessionAttributes(attributes);
		
			
					// close the skill
					logger.info("XXXXX CLOSE SKILL");
					
					String speechText = "OK thanks, <say-as interpret-as=\"interjection\">goodbye!</say-as>";
					
					return input.getResponseBuilder()
					.withSpeech(speechText)
					.build();
				
				}
			}
			else if (doAnythingElseQ) {
			
				// if user says yes they want to add another item to current event, if they say no then they have finished adding items and the event is submitted
				
				if (isYes==true) {
				
					// ask them to add items
					attributesManager.setSessionAttributes(attributes);
					return SessionContentChecker.getResponse(input, attributes, false, "OK, ");
				
				}
				else if (isYes==false) {
				
					// user does not want to add anything else to the current event
					// so submit event
								
					attributes = SubmitEventIntentHandler.saveEventAndCreateNext(event, attributes);
				
					prefix = "OK, event submitted. ";
					
					attributesManager.setSessionAttributes(attributes);
					return SessionContentChecker.getResponse(input, attributes, true, prefix);
				}
			
			}
			
		}
		
		
		// found the YesNoIntent but not a yes/no or not following a gap /new event question
			
		logger.info("XXXXX YesNoIntentHandler.handle DEFAULT_YESNOINTENT");
			
		event.addBreadcrumb("DEFAULT_YESNOINTENT");
		attributes.put("current_event", event.getByteString());
		attributesManager.setSessionAttributes(attributes);
			
		return SessionContentChecker.getResponse(input, attributes, false);
	
	}


	/*
	* takes the yesOrNo slot value and creates a Boolean denoting if the slot
	* contains yes (versus no), or null if it has no value
	*/
	public static Boolean isYes(Optional<String> yesOrNo) {
	
		if (yesOrNo.isPresent() == false) {
			return null;
		}
		else {
		
			String [] yesNoPhraseParts = yesOrNo.get().split(" ");
			List<String> words = Arrays.asList(yesNoPhraseParts);
			
			if (words.contains("yes")) {
				return true;
			}
			else if (words.contains("no")) {
			
				return false;
			}
			
		}
		
		return null;
	
	}

	
}
