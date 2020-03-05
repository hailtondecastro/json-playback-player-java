package org.jsonplayback.player.hibernate;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Key for components and components relations by path from owner entity.<br>
 * Internal use! 
 * 
 * @author Hailton
 *
 */
public class AssociationAndComponentPathKey {
	private Class<?> entityRootOrComponentClassOwner;
	private String pathFromOwner;

	public AssociationAndComponentPathKey(Class<?> entityRootOrComponentClassOwner, String pathFromOwner) {
		super();
		this.entityRootOrComponentClassOwner = entityRootOrComponentClassOwner;
		this.pathFromOwner = pathFromOwner;
	}

	public Class<?> getEntityClassRootOwner() {
		return entityRootOrComponentClassOwner;
	}

	public String getPathFromOwner() {
		return pathFromOwner;
	}

	@Override
	public int hashCode() {
		return this.entityRootOrComponentClassOwner.hashCode() + this.pathFromOwner.hashCode();				
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AssociationAndComponentPathKey) {
			AssociationAndComponentPathKey entryObj = (AssociationAndComponentPathKey) obj;
			return this.entityRootOrComponentClassOwner.equals(entryObj.getEntityClassRootOwner());
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LinkedHashMap<String, Object> thisAsMap = new LinkedHashMap<>();
			thisAsMap.put("entityRootOrComponentClassOwner", this.entityRootOrComponentClassOwner.getName());
			thisAsMap.put("pathFromOwner", this.pathFromOwner);
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(thisAsMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
}
