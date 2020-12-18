package org.jsonplayback.player.implementation;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsonplayback.player.IdentityRefKey;
import org.jsonplayback.player.PlayerMetadatas;
import org.jsonplayback.player.PlayerSnapshot;
import org.jsonplayback.player.SignatureBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PlayerJsonGeneratorDelegate extends JsonGeneratorDelegate {
	private static Logger logger = LoggerFactory.getLogger(PlayerJsonGeneratorDelegate.class);
	
	IPlayerManagersHolderImplementor managersHolder;
	private SerializerProvider serializers;

	public PlayerJsonGeneratorDelegate configSerializers(SerializerProvider serializers) {
		this.serializers = serializers;
		return this;
	}

	public PlayerJsonGeneratorDelegate configManagerHolder(IPlayerManagersHolderImplementor managersHolder) {
		this.managersHolder = managersHolder;
		return this;
	}
	
	public PlayerJsonGeneratorDelegate(JsonGenerator d) {
		super(d);
	}
	
	@Override
	public void writeStartObject(Object forValue) throws IOException {
		IPlayerManagerImplementor manager = this.managersHolder.getStartedManagerImplementor();
		if (!Map.class.isAssignableFrom(forValue.getClass())
				&& !Collection.class.isAssignableFrom(forValue.getClass())
				&& !Array.class.isAssignableFrom(forValue.getClass())) {
			if (!manager.isManagerOfType(forValue.getClass())) {
				throw new RuntimeException("Started manager ("+manager.getConfig().getManagerId()+") is not manager of type " + manager.getConfig().getObjPersistenceSupport().unwrappRealType(forValue));
			}
		}
		this.delegate.writeStartObject(forValue);
		if (!this.managersHolder.thereIsStartedManager()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Not Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). !this.managersHolder.thereIsStartedManager()");
			}
			this.delegate.writeStartObject();
		} else if (forValue instanceof PlayerSnapshot) {
			if (logger.isTraceEnabled()) {
				logger.trace("Not Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). forValue instanceof PlayerSnapshot");
			}
			this.delegate.writeStartObject();
		} else if (this.managersHolder.getStartedManagerImplementor().getIdByObjectMap().containsKey(new IdentityRefKey(forValue))) {
			throw new RuntimeException(MessageFormat.format("Serializing an object that has been serialized and referenced. {0}: {1}", this.managersHolder.getStartedManagerImplementor().getIdByObjectMap().get(forValue), forValue));
		} else {
			PlayerBeanPropertyWriter currPropertyWriter = null;
			if (this.managersHolder.getStartedManagerImplementor().getPlayerBeanPropertyWriterStepStack().size() > 0) {
				currPropertyWriter = this.managersHolder.getStartedManagerImplementor().getPlayerBeanPropertyWriterStepStack().peek();
			}
			
			this.managersHolder.getStartedManagerImplementor().currIdPlusPlus();
			this.managersHolder.getStartedManagerImplementor().getObjectByIdMap().put(this.managersHolder.getStartedManagerImplementor().getCurrId(), forValue);
			this.managersHolder.getStartedManagerImplementor().getIdByObjectMap().put(new IdentityRefKey(forValue), this.managersHolder.getStartedManagerImplementor().getCurrId());
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). Setting \"{0}\": {1}",
						"playerMetadatas.id", this.managersHolder.getStartedManagerImplementor().getCurrId()));
			}
			PlayerMetadatas playerMetadatas = manager.getConfig().getMetadataInstantiator().apply(manager);
			playerMetadatas.setId(this.managersHolder.getStartedManagerImplementor().getCurrId());
			if (this.managersHolder.getStartedManagerImplementor().isPersistentClass(forValue.getClass())) {
				SignatureBean signatureBean = this.managersHolder.getStartedManagerImplementor().generateSignature(forValue);
				String signatureStr = this.managersHolder.getStartedManagerImplementor().serializeSignature(signatureBean);
				if (logger.isTraceEnabled()) {
					logger.trace(MessageFormat.format(
							"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). It is a persistent class. Setting \"{0}\": \"{1}\"",
							"playerMetadatas.signature", signatureStr));
				}
				playerMetadatas.setSignature(signatureStr);
				if (currPropertyWriter != null) {
					if (logger.isTraceEnabled()) {
						logger.trace(MessageFormat.format(
								"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). It is an associative class property. Setting \"{0}\": \"{1}\"",
								"playerMetadatas.isAssociative", true));
					}
					playerMetadatas.setIsAssociative(true);
				}
				this.managersHolder.getStartedManagerImplementor().getPlayerJsonSerializerStepStack().peek().findPlayerObjectId(forValue, this, serializers, playerMetadatas);
			} else {
				AssociationAndComponentTrackInfo aacTrackInfo = this.managersHolder.getStartedManagerImplementor().getCurrentAssociationAndComponentTrackInfo();
				//if (aacTrackInfo != null && !this.managersHolder.getStartedManagerImplementor().isNeverSigned(forValue.getClass())) {
				if (aacTrackInfo != null) {
					String signatureStr = null;
					if (currPropertyWriter != null
							&& !currPropertyWriter.isMetadatasPlayerObjectId()
							&& !currPropertyWriter.getIsPlayerObjectId()) {
						SignatureBean signatureBean = this.managersHolder.getStartedManagerImplementor().generateComponentSignature(aacTrackInfo);
						signatureStr = this.managersHolder.getStartedManagerImplementor().serializeSignature(signatureBean);
						playerMetadatas.setSignature(signatureStr);						
					}
					playerMetadatas.setIsComponent(true);
					if (logger.isTraceEnabled()) {
						if (logger.isTraceEnabled()) {
							Map<String, Object> anyLogMap = new LinkedHashMap<>();
							if (signatureStr != null) {
								anyLogMap.put("playerMetadatas.signature", signatureStr);								
							}
							anyLogMap.put("playerMetadatas.isComponent", true);
							String jsonLogMsg = this.generateJsonStringForLog(anyLogMap);
							jsonLogMsg = jsonLogMsg.substring(1, jsonLogMsg.length() - 1);
							String logMsg =
								MessageFormat.format(
									"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). It is a componnent class property. Setting:\n"
											+ "{0}",
											jsonLogMsg
									); 
							logger.trace(logMsg);
						}
						
					}
					this.managersHolder.getStartedManagerImplementor().getPlayerJsonSerializerStepStack().peek().findPlayerObjectId(forValue, this, serializers, playerMetadatas);
				} else {
				}
			}
			
			
			//I can be writing a PlayerMetadatas.playerObjectId  
			if (forValue != null
					&& this.managersHolder.getStartedManagerImplementor().isComponent(forValue.getClass())
					&& this.managersHolder.getStartedManagerImplementor().getPlayerMetadatasWritingStack().size() > 0
					&& forValue == this.managersHolder.getStartedManagerImplementor().getPlayerMetadatasWritingStack().peek().getPlayerObjectId()) {
				playerMetadatas.setIsComponent(true);
				playerMetadatas.setIsComponentPlayerObjectId(true);
			}
			
			try {
				this.managersHolder.getStartedManagerImplementor().getPlayerMetadatasWritingStack().push(playerMetadatas);
				this.writeFieldName(this.managersHolder.getStartedManagerImplementor().getConfig().getPlayerMetadatasName());
				this.writeObject(playerMetadatas);				
			} finally {
				if (this.managersHolder.getStartedManagerImplementor().getPlayerMetadatasWritingStack() != null) {
					PlayerMetadatas playerMetadatasPoped = this.managersHolder.getStartedManagerImplementor().getPlayerMetadatasWritingStack().pop();
					if (playerMetadatasPoped != playerMetadatas) {
						throw new RuntimeException("This should not happen");
					}					
				}
			}
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). Injecting field \"{0}\": {1}",
						this.managersHolder.getStartedManagerImplementor().getConfig().getPlayerMetadatasName(), playerMetadatas));
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private String generateJsonStringForLog(Map anyMap) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(anyMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
}
/*gerando conflito*/