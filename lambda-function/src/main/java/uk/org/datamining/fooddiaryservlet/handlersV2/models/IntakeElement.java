package uk.org.datamining.fooddiaryservlet.handlersV2.models;


import java.io.*;
import java.util.Optional;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.amazon.ask.attributes.AttributesManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.ask.model.Slot;

public class IntakeElement implements Serializable {
	
	private static final long serialversionUID = 1234L;
	static Logger logger = LoggerFactory.getLogger(IntakeElement.class);

	//protected Integer quantity;
	//protected String quantityType;
	protected String description;
	//protected String quantityWordy;
	protected Boolean validQuantity;
	protected Boolean validFoodDrink;
	
	public String toSpokenString() {

		String sessionContents = "You had ";
	
		// drank something but we don't know what
		String description = getDescription();
		if (description==null) {
			
			sessionContents += " something.";
		}
		else {
			sessionContents +=description;
		}
		/*
		Integer quantity = getQuantity();
		if (quantity!=null) {
			sessionContents += "Quantity " + quantity + ".";
		}
	
		String quantityType = getQuantityType();
		if (quantityType!=null) {
			sessionContents += "Quantity type " + quantityType + ".";
		}
		*/
		return sessionContents;
		
	}
	
	/*
	public boolean hasQuantity() {
		return quantityWordy!=null || quantityType!=null || quantity!=null;
	}
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getQuantity() {
		return quantity;
	}
	
	public void setQuantityType(String quantityType) {
		this.quantityType = quantityType;
	}
	public String getQuantityType() {
		return quantityType;
	}
	*/
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	/*
	public void setQuantityWordy(String quantityWordy) {
		this.quantityWordy = quantityWordy;
	}
	
	public String getQuantityWordy() {
		return quantityWordy;
	}*/
	
	public String toString() {
		return "validQuantity[" + getValidQuantity() + "];  validFoodDrink[" + getValidFoodDrink() + "]; description[" + getDescription() + "]";
	}
	
	public boolean isComplete() {
	
		if (description!=null)
			return true;
		else
			return false;
	}
	
	/*
	public void setGenericQuantity(HandlerInput input) {
	
		RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
		
		Optional<String> genericQuantity = requestHelper.getSlotValue("generic_quantity");
		Optional<String> singularA = requestHelper.getSlotValue("singular_a");
		Optional<String> quantityWordy = requestHelper.getSlotValue("quantity_wordy");
		
		setGenericQuantity(input, genericQuantity, singularA, quantityWordy);
	}
	
	public void setGenericQuantity(HandlerInput input, Optional<String> genericQuantity, Optional<String> singularA, Optional<String> quantityWordy) {
		String speechText = "generic_quantity: " + genericQuantity
			+ ", singular_a: " + singularA
			+ ", quantity_wordy: " + quantityWordy;
			
        logger.info("XXXXXXXXXXXXXXXXXXXXX input " + speechText);
		
		AttributesManager attributesManager = input.getAttributesManager();
		Map<String,Object> attributes = attributesManager.getSessionAttributes();
		
		if (quantityWordy.isPresent()) {
			setQuantityWordy(quantityWordy.get());
		}
		
		if (genericQuantity.isPresent() ) {
			setQuantity(Integer.parseInt(genericQuantity.get()));
		}
		else if (singularA.isPresent()) {
			// e.g. a glass of water, drinkQuantity=glass,quantity=one
			setQuantity(1);
		}
		
	}*/
	
	public void setValidQuantity(Boolean validQuantity) {
		this.validQuantity = validQuantity;
	}
	
	public void setValidFoodDrink(Boolean validFoodDrink) {
		this.validFoodDrink = validFoodDrink;
	}
	
	public Boolean getValidQuantity() {
		return validQuantity;
	}
	
	public Boolean getValidFoodDrink() {
		return validFoodDrink;
	}
	
}

