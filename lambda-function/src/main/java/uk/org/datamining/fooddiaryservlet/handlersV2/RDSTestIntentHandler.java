package uk.org.datamining.fooddiaryservlet.handlersV2;

//import com.amazon.ask.airplanefacts.util.FactsUtil;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.display.*;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;

import static com.amazon.ask.request.Predicates.intentName;


import com.amazonaws.services.lambda.runtime.Context;


import uk.org.datamining.fooddiaryservlet.handlersV2.models.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDSTestIntentHandler implements RequestHandler {

    private static Logger logger = LoggerFactory.getLogger(RDSTestIntentHandler.class);


	@Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("RDSTestIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
    
    	logger.info("XXXXXXXXXXXXXXXXXXXXX RDSTestIntentHandler.handle");

		DatabaseInputOutputTool.dbTest();

		String speechText = "test complete";

        return input.getResponseBuilder()
			.withSpeech(speechText)
            //.withSimpleCard(title, primaryText)
            .withReprompt(speechText)
            .build();
    }

}
