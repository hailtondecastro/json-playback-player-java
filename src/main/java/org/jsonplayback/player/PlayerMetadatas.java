package org.jsonplayback.player;

import java.util.LinkedHashMap;

import org.jsonplayback.player.implementation.PlayerBeanPropertyWriter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used to Serialize all metadata.
 * @author Hailton de Castro
 *
 */
public class PlayerMetadatas implements Cloneable {
	@JsonProperty("$iAmPlayerMetadatas$")
	@JsonInclude(Include.NON_DEFAULT)
	private Boolean iAmPlayerMetadatas = true;
	
	@JsonProperty("$id$")
	@JsonInclude(Include.NON_NULL)
	private Long id;
	
	@JsonInclude(Include.NON_NULL)
	@JsonProperty("$idRef$")
	private Long idRef;
	
	@JsonProperty("$signature$")
	private String signature;
	
	@JsonProperty("$isLazyUninitialized$")
	@JsonInclude(Include.NON_DEFAULT)
	private Boolean isLazyUninitialized = false;
	
	@JsonProperty("$isComponent$")
	@JsonInclude(Include.NON_DEFAULT)
	private Boolean isComponent = false;
	
	@JsonProperty("$isComponentPlayerObjectId$")
	@JsonInclude(Include.NON_DEFAULT)
	private Boolean isComponentPlayerObjectId = false;
	
	@JsonProperty("$isAssociative$")
	@JsonInclude(Include.NON_DEFAULT)
	private Boolean isAssociative = false;
	
	@JsonProperty("$isLazyProperty$")
	@JsonInclude(Include.NON_DEFAULT)
	private Boolean isLazyProperty = false;
	
	@JsonProperty("$playerObjectId$")
	@JsonInclude(Include.NON_NULL)
	private Object playerObjectId;

	@JsonIgnore
	private PlayerBeanPropertyWriter originalPlayerObjectIdPropertyWriter;
	@JsonIgnore
	private Object originalPlayerObjectIdOwner;
	
	public Object getOriginalPlayerObjectIdOwner() {
		return originalPlayerObjectIdOwner;
	}
	public void setOriginalPlayerObjectIdOwner(Object originalPlayerObjectIdOwner) {
		this.originalPlayerObjectIdOwner = originalPlayerObjectIdOwner;
	}
	public PlayerBeanPropertyWriter getOriginalPlayerObjectIdPropertyWriter() {
		return originalPlayerObjectIdPropertyWriter;
	}
	public void setOriginalPlayerObjectIdPropertyWriter(PlayerBeanPropertyWriter originalPlayerObjectIdPropertyWriter) {
		this.originalPlayerObjectIdPropertyWriter = originalPlayerObjectIdPropertyWriter;
	}
	public Boolean getIsComponentPlayerObjectId() {
		return isComponentPlayerObjectId;
	}
	public void setIsComponentPlayerObjectId(Boolean isComponentPlayerObjectId) {
		this.isComponentPlayerObjectId = isComponentPlayerObjectId;
	}
	public Boolean getiAmPlayerMetadatas() {
		return iAmPlayerMetadatas;
	}
	public void setiAmPlayerMetadatas(Boolean iAmPlayerMetadatas) {
		this.iAmPlayerMetadatas = iAmPlayerMetadatas;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getIdRef() {
		return idRef;
	}
	public void setIdRef(Long idRef) {
		this.idRef = idRef;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public Boolean getIsLazyUninitialized() {
		return isLazyUninitialized;
	}
	public void setIsLazyUninitialized(Boolean isLazyUninitialized) {
		this.isLazyUninitialized = isLazyUninitialized;
	}
	public Boolean getIsComponent() {
		return isComponent;
	}
	public void setIsComponent(Boolean isComponent) {
		this.isComponent = isComponent;
	}
	public Boolean getIsAssociative() {
		return isAssociative;
	}
	public void setIsAssociative(Boolean isAssociative) {
		this.isAssociative = isAssociative;
	}
	public Boolean getIsLazyProperty() {
		return isLazyProperty;
	}
	public void setIsLazyProperty(Boolean isLazyProperty) {
		this.isLazyProperty = isLazyProperty;
	}
	public Object getPlayerObjectId() {
		return playerObjectId;
	}
	public void setPlayerObjectId(Object playerObjectId) {
		this.playerObjectId = playerObjectId;
	}
	
	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LinkedHashMap<String, Object> thisAsMap = new LinkedHashMap<>();
			thisAsMap.put("iAmPlayerMetadatas", this.iAmPlayerMetadatas);
			thisAsMap.put("id", this.id);
			thisAsMap.put("idRef", this.idRef);
			thisAsMap.put("signature", this.signature);
			thisAsMap.put("isLazyUninitialized", this.isLazyUninitialized);
			thisAsMap.put("isComponent", this.isComponent);
			thisAsMap.put("isComponentPlayerObjectId", this.isComponentPlayerObjectId);
			thisAsMap.put("isAssociative", this.isAssociative);
			thisAsMap.put("isLazyProperty", this.isLazyProperty);
			thisAsMap.put("playerObjectId", this.playerObjectId != null? this.playerObjectId.toString() : null);
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(thisAsMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
	
	protected PlayerMetadatas clone() {
		PlayerMetadatas metadatasClone = new PlayerMetadatas();
		metadatasClone.iAmPlayerMetadatas           = this.iAmPlayerMetadatas          ;       
		metadatasClone.id                                = this.id                               ;      
		metadatasClone.idRef                             = this.idRef                            ;      
		metadatasClone.signature                         = this.signature                        ;      
		metadatasClone.isLazyUninitialized               = this.isLazyUninitialized              ;      
		metadatasClone.isComponent                       = this.isComponent                      ;      
		metadatasClone.isComponentPlayerObjectId            = this.isComponentPlayerObjectId           ;      
		metadatasClone.isAssociative                     = this.isAssociative                    ;      
		metadatasClone.isLazyProperty                    = this.isLazyProperty                   ;      
		metadatasClone.playerObjectId                       = this.playerObjectId                      ;      
		metadatasClone.originalPlayerObjectIdPropertyWriter = this.originalPlayerObjectIdPropertyWriter;      
		metadatasClone.originalPlayerObjectIdOwner          = this.originalPlayerObjectIdOwner         ;      
		return metadatasClone;
	}
}