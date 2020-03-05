package org.jsonplayback.player;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SignatureBean {
	private String signature;
	private Class<?> clazz;
	private String entityName;
	private Boolean isColl = false;
//	private Boolean isAssoc = false;
	private Boolean isComp = false;
	private Boolean isLazyProperty = false;
	private String stringifiedObjectId;
	/**
	 * Se nulo eh a propria entidade, caso contrario eh o lazy de uma entidade.
	 */
	private String propertyName;
	//private Object[] rawKeyValues;
	
	public Boolean getIsLazyProperty() {
		return isLazyProperty;
	}
	public void setIsLazyProperty(Boolean isLazyProperty) {
		this.isLazyProperty = isLazyProperty;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public String getStringifiedObjectId() {
		return stringifiedObjectId;
	}
	public void setStringifiedObjectId(String stringifiedObjectId) {
		this.stringifiedObjectId = stringifiedObjectId;
	}
//	public Object[] getRawKeyValues() {
//		return rawKeyValues;
//	}
//	public void setRawKeyValues(Object[] rawKeyValues) {
//		this.rawKeyValues = rawKeyValues;
//	}
	public Boolean getIsColl() {
		return isColl;
	}
	public void setIsColl(Boolean isColl) {
		this.isColl = isColl;
	}
//	public Boolean getIsAssoc() {
//		return isAssoc;
//	}
//	public void setIsAssoc(Boolean isAssoc) {
//		this.isAssoc = isAssoc;
//	}
	public Boolean getIsComp() {
		return isComp;
	}
	public void setIsComp(Boolean isComp) {
		this.isComp = isComp;
	}
	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LinkedHashMap<String, Object> thisAsMap = new LinkedHashMap<>();
			thisAsMap.put("signature",      this.getSignature()   );
			thisAsMap.put("clazz",          this.getClazz()       );
			thisAsMap.put("entityName",     this.getEntityName()  );
			thisAsMap.put("propertyName",   this.getPropertyName());
			thisAsMap.put("stringifiedObjectId",   this.getStringifiedObjectId());
//			thisAsMap.put("isAssoc",        this.getIsAssoc());
			thisAsMap.put("isColl",         this.getIsColl());
			thisAsMap.put("isComp",         this.getIsComp());
			thisAsMap.put("isLazyProperty",         this.getIsLazyProperty());
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(thisAsMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Isso nao deveria acontecer", e);
		}
	}
}
/*gerando conflito*/