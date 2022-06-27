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
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;

import java.util.Optional;
import java.util.Map;

import com.amazon.ask.attributes.AttributesManager;

import static com.amazon.ask.request.Predicates.requestType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;


public class LaunchRequestHandler implements RequestHandler {

    private static Logger logger = LoggerFactory.getLogger(LaunchRequestHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
	
		logger.info("XXXXXXXXXXXXXXXXXXXXX LaunchRequestHandler.handle");

		// add breadcrumb to event
		AttributesManager attributesManager = input.getAttributesManager();
		Map<String,Object> attributes = attributesManager.getSessionAttributes();	
		IntakeEvent event = IntakeEvent.bytesToEvent((String)attributes.get("current_event"));
		event.addBreadcrumb("LAUNCH");
		
		//Boolean first_time = DatabaseInputOutputTool.getFirstTime();
		//attributes.put("first_time", first_time);

		//String speechText = "Welcome to the food and drink diary! When did you have something?";
		//if (first_time == false) {
		String speechText = "Hi, when did you have something? ";
		//}
		String repromptText = "Please let me know when you ate or drank! ";

		
		String deviceId = input.getRequestEnvelope().getContext().getSystem().getDevice().getDeviceId();
		event.setDeviceID(deviceId);
		
		String userId = input.getRequestEnvelope().getContext().getSystem().getUser().getUserId();
		event.setUserID(userId);
		
		//logger.info("XXXXXXXXXXXXXXXXXXXXX DEVICE ID ******* "  + deviceId);
		//logger.info("XXXXXXXXXXXXXXXXXXXXX USER ID ******* "  + userId);
		
		
		
		attributes.put("current_event", event.getByteString());
		
		
		
		attributesManager.setSessionAttributes(attributes);
		
		
        return input.getResponseBuilder()
                .withSpeech(speechText)
                //.withSimpleCard("HelloWorld", speechText)
                .withReprompt(repromptText)
                .build();
    }


}
