package org.jsonplayback.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.jsonplayback.player.implementation.AssociationAndComponentPath;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssociationAndComponentPathHbSupport extends AssociationAndComponentPath {
	private CompositeType[] compositeTypePath;
	private CollectionType collType;
	private CompositeType compType;	
	private EntityType relEntity;
	public EntityType getRelEntity() {
		return relEntity;
	}
	public void setRelEntity(EntityType relEntity) {
		this.relEntity = relEntity;
	}
	public CompositeType getCompType() {
		return compType;
	}
	public void setCompType(CompositeType compType) {
		this.compType = compType;
	}
	public CollectionType getCollType() {
		return collType;
	}
	public void setCollType(CollectionType collType) {
		this.collType = collType;
	}
	public CompositeType[] getCompositeTypePath() {
		return compositeTypePath;
	}
	public void setCompositeTypePath(CompositeType[] compositeTypePath) {
		this.compositeTypePath = compositeTypePath;
	}
	
	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			ArrayList<String> compositeTypePathStrList = new ArrayList<>();
			for (CompositeType compositeTypeItem : this.compositeTypePath) {
				compositeTypePathStrList.add(
						"{ owner class: " + this.getAacKey().getEntityClassRootOwner().getName() + 
						", component class: " + compositeTypeItem.getReturnedClass().getName() +
						", properties: " + Arrays.toString(compositeTypeItem.getPropertyNames()) + 
						" }"
						);
			}
			
			LinkedHashMap<String, Object> thisAsMap = new LinkedHashMap<>();
			thisAsMap.put("componentTypeKey", this.getAacKey());
			thisAsMap.put("compositeTypePath", compositeTypePathStrList);
			thisAsMap.put("compositePrpPath", this.getCompositePrpPath());
			thisAsMap.put("collType", this.getCollType() != null? this.collType.toString(): null);
			thisAsMap.put("relEntity", this.getRelEntity() != null? this.getRelEntity().toString(): null);
			thisAsMap.put("compType", this.compType != null? this.compType.toString(): null);
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(thisAsMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
}
