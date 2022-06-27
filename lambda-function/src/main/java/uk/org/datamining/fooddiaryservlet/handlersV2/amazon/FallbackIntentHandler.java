package uk.org.datamining.fooddiaryservlet.handlersV2.amazon;


import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.attributes.AttributesManager;

import java.util.Optional;
import java.util.Map;

import uk.org.datamining.fooddiaryservlet.handlersV2.*;

import static com.amazon.ask.request.Predicates.intentName;

import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FallbackIntentHandler implements RequestHandler {

	static Logger logger = LoggerFactory.getLogger(FallbackIntentHandler.class);
	
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.FallbackIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        
        logger.info("XXXXXXXXXXXXXXXXXXXXX FallbackIntentHandler.handle");
        
        AttributesManager attributesManager = input.getAttributesManager();
		Map<String,Object> attributes = attributesManager.getSessionAttributes();
		
		IntakeEvent event = IntakeEvent.bytesToEvent((String)attributes.get("current_event"));

        return SessionContentChecker.getResponse(input, attributes, false);
        
    }
}
