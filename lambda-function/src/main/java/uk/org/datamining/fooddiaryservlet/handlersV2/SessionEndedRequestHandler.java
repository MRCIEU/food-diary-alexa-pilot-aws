package uk.org.datamining.fooddiaryservlet.handlersV2;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.SessionEndedRequest;
import com.amazon.ask.request.RequestHelper;
import com.amazon.ask.attributes.AttributesManager;

import java.util.Optional;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;

import static com.amazon.ask.request.Predicates.requestType;

public class SessionEndedRequestHandler implements RequestHandler {

	static Logger logger = LoggerFactory.getLogger(SessionEndedRequestHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(requestType(SessionEndedRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        // any cleanup logic goes here
        
        logger.info("XXXXXXXXXXXXXXXXXXXXX SessionEndedRequestHandler.handle");

        // store the event if it is incomplete / user hasn't submitted it
        
        AttributesManager attributesManager = input.getAttributesManager();
		Map<String,Object> attributes = attributesManager.getSessionAttributes();

		logger.info("XXXXX " + attributes.toString());
	
		IntakeEvent event = IntakeEvent.bytesToEvent((String)attributes.get("current_event"));
	
		if (!event.isEmpty()) {
			event.addBreadcrumb("SESSIONEND");
	        DatabaseInputOutputTool.addAlexaIntakeEvent(event, true);
        }

        return input.getResponseBuilder().build();
    }

}
