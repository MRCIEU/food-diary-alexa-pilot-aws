package uk.org.datamining.fooddiaryservlet;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.SkillStreamHandler;

import uk.org.datamining.fooddiaryservlet.handlersV2.*;
import uk.org.datamining.fooddiaryservlet.handlersV2.amazon.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FoodDiaryWholeInputSkillHandler extends SkillStreamHandler {

    private static Skill getSkill() {
        return Skills.standard()
                .addRequestHandlers(
                        new CancelandStopIntentHandler(),
                        new ClearConsumptionEventIntent(),
                        new ClearLastItemIntent(),
                        new IntakeIntentHandler(),
				new ConsumptionDateAndTimeIntentHandler(),
				//new UserIDIntentHandler(),
				new YesNoIntentHandler(),
				new SubmitEventIntentHandler(),
                        
                new RDSTestIntentHandler(),
                        new FactIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new SessionEndedRequestHandler(),
                        new FallbackIntentHandler())
                // Add your skill id below and uncomment to enable skill ID verification
                // .withSkillId("")

                .build();
    }

    public FoodDiaryWholeInputSkillHandler() {
        super(getSkill());
        
        
    }

}
