package org.jsonplayback.player.implementation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.jsonplayback.player.Tape;
import org.jsonplayback.player.TapeAction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;

public class TapeDefault extends Tape {
	/**
	 * Cache!
	 */
	protected DefaultDeserializationContext defaultDeserializationContext;
	
	public DefaultDeserializationContext getDefaultDeserializationContext() {
		return defaultDeserializationContext;
	}

	public void setDefaultDeserializationContext(DefaultDeserializationContext defaultDeserializationContext) {
		this.defaultDeserializationContext = defaultDeserializationContext;
	}
	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LinkedHashMap<String, Object> thisAsMap = new LinkedHashMap<>();
			ArrayList<Object> actionList = new ArrayList<>();
			for (TapeAction actionItem : this.getActions()) {
				TapeActionDefault tapeActionItem = (TapeActionDefault) actionItem;
				
				LinkedHashMap<String, Object> actionAsMap = new LinkedHashMap<>();
				actionAsMap.put("ownerSignatureStr", tapeActionItem.getOwnerSignatureStr());
				actionAsMap.put("ownerCreationId", tapeActionItem.getOwnerCreationId());
				actionAsMap.put("ownerCreationRefId", tapeActionItem.getOwnerCreationRefId());
				actionAsMap.put("settedSignatureStr", tapeActionItem.getSettedSignatureStr());
				actionAsMap.put("settedCreationId", tapeActionItem.getSettedCreationId());
				actionAsMap.put("settedCreationRefId", tapeActionItem.getSettedCreationRefId());
				actionAsMap.put("ownerPlayerType", tapeActionItem.getOwnerPlayerType());
				actionAsMap.put("actionType", tapeActionItem.getActionType());
				actionAsMap.put("fieldName", tapeActionItem.getFieldName());
				actionAsMap.put("simpleSettedValue", tapeActionItem.getSimpleSettedValue());
				actionList.add(actionAsMap);
			}
			thisAsMap.put("actions", actionList);
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(thisAsMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
}
/* gerando conflito */