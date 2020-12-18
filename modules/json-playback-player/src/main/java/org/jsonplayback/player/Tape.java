package org.jsonplayback.player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;

public class Tape {
	private List<TapeAction> actions;

	public List<TapeAction> getActions() {
		return actions;
	}

	public void setActions(List<TapeAction> actions) {
		this.actions = actions;
	}

	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LinkedHashMap<String, Object> thisAsMap = new LinkedHashMap<>();
			ArrayList<Object> actionList = new ArrayList<>();
			for (TapeAction actionItem : this.actions) {
				LinkedHashMap<String, Object> actionAsMap = new LinkedHashMap<>();
				actionAsMap.put("ownerSignatureStr", actionItem.getOwnerSignatureStr());
				actionAsMap.put("ownerCreationId", actionItem.getOwnerCreationId());
				actionAsMap.put("ownerCreationRefId", actionItem.getOwnerCreationRefId());
				actionAsMap.put("settedSignatureStr", actionItem.getSettedSignatureStr());
				actionAsMap.put("settedCreationId", actionItem.getSettedCreationId());
				actionAsMap.put("settedCreationRefId", actionItem.getSettedCreationRefId());
				actionAsMap.put("ownerPlayerType", actionItem.getOwnerPlayerType());
				actionAsMap.put("actionType", actionItem.getActionType());
				actionAsMap.put("fieldName", actionItem.getFieldName());
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