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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


import static com.amazon.ask.request.Predicates.intentName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;

public class ClearLastItemIntent implements RequestHandler {

	static Logger logger = LoggerFactory.getLogger(ClearLastItemIntent.class);

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ClearLastItemIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {

		logger.info("XXXXXXXXXXXXXXXXXXXXX ClearLastItemIntent.handle");


		// map input to right consumption type
		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		
		AttributesManager attributesManager = input.getAttributesManager();
		Map<String,Object> attributes = attributesManager.getSessionAttributes();

		IntakeEvent event = IntakeEvent.bytesToEvent((String)attributes.get("current_event"));
		
		logger.info("XXXXXXXXXXXXXXXXXXXXX " + event.toString());
		
		
		
		event.addBreadcrumb("CLEAR_LAST_ITEM");
		
		String userID = event.getUserID();
		String deviceID = event.getDeviceID();
		
		String prefixText = "";
		
		// remove last item from event
		List<IntakeElement> elements = event.getIntakeElements();
		if (elements.size()>0) {
			elements.remove(elements.size()-1);
			event.setIntakeElements(elements);
			
			prefixText = "OK, item removed. ";
		}
		else {
			prefixText = "You don't have any items in this event. ";
		}
		
	 	
		attributes.put("current_event", event.getByteString());
		attributesManager.setSessionAttributes(attributes);
		
		
		
		
		return SessionContentChecker.getResponse(input, attributes, true, prefixText);
		
    }

}
