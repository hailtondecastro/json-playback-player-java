package org.jsonplayback.player.implementation;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.IdentityRefKey;
import org.jsonplayback.player.PlayerSnapshot;
import org.jsonplayback.player.PlayerMetadatas;
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
	
	IPlayerManagerImplementor manager;
	private SerializerProvider serializers;

	public PlayerJsonGeneratorDelegate configSerializers(SerializerProvider serializers) {
		this.serializers = serializers;
		return this;
	}

	public PlayerJsonGeneratorDelegate configManager(IPlayerManagerImplementor manager) {
		this.manager = manager;
		return this;
	}
	
	public PlayerJsonGeneratorDelegate(JsonGenerator d) {
		super(d);
	}
	
	@Override
	public void writeStartObject(Object forValue) throws IOException {
		this.delegate.writeStartObject(forValue);
		if (!this.manager.isStarted()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Not Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). !this.manager.isStarted()");
			}
			this.delegate.writeStartObject();
		} else if (forValue instanceof PlayerSnapshot) {
			if (logger.isTraceEnabled()) {
				logger.trace("Not Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). forValue instanceof PlayerSnapshot");
			}
			this.delegate.writeStartObject();
		} else if (this.manager.getIdByObjectMap().containsKey(new IdentityRefKey(forValue))) {
			throw new RuntimeException(MessageFormat.format("Serializing an object that has been serialized and referenced. {0}: {1}", this.manager.getIdByObjectMap().get(forValue), forValue));
		} else {
			this.manager.currIdPlusPlus();
			this.manager.getObjectByIdMap().put(this.manager.getCurrId(), forValue);
			this.manager.getIdByObjectMap().put(new IdentityRefKey(forValue), this.manager.getCurrId());
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). Setting \"{0}\": {1}",
						"backendMetadatas.id", this.manager.getCurrId()));
			}
			PlayerMetadatas backendMetadatas = new PlayerMetadatas();
			backendMetadatas.setId(this.manager.getCurrId());
			
			if (this.manager.isPersistentClass(forValue.getClass()) && !this.manager.isNeverSigned(forValue.getClass())) {
				SignatureBean signatureBean = this.manager.generateSignature(forValue);
				String signatureStr = this.manager.serializeSignature(signatureBean);
				if (logger.isTraceEnabled()) {
					logger.trace(MessageFormat.format(
							"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). It is a persistent class. Setting \"{0}\": \"{1}\"",
							"backendMetadatas.signature", signatureStr));
				}
				backendMetadatas.setSignature(signatureStr);
				if (this.manager.getPlayerBeanPropertyWriterStepStack().size() > 0) {
					if (logger.isTraceEnabled()) {
						logger.trace(MessageFormat.format(
								"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). It is an associative class property. Setting \"{0}\": \"{1}\"",
								"backendMetadatas.isAssociative", true));
					}
					backendMetadatas.setIsAssociative(true);
				}
				this.manager.getPlayerJsonSerializerStepStack().peek().findPlayerObjectId(forValue, this, serializers, backendMetadatas);
			} else {
				AssociationAndComponentTrackInfo aacTrackInfo = this.manager.getCurrentAssociationAndComponentTrackInfo();
				if (aacTrackInfo != null && !this.manager.isNeverSigned(forValue.getClass())) {
					SignatureBean signatureBean = this.manager.generateComponentSignature(aacTrackInfo);
					String signatureStr = this.manager.serializeSignature(signatureBean);
					if (logger.isTraceEnabled()) {
						if (logger.isTraceEnabled()) {
							Map<String, Object> anyLogMap = new LinkedHashMap<>();
							anyLogMap.put("backendMetadatas.signature", signatureStr);
							anyLogMap.put("backendMetadatas.isComponent", true);
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
					backendMetadatas.setSignature(signatureStr);
					backendMetadatas.setIsComponent(true);
					this.manager.getPlayerJsonSerializerStepStack().peek().findPlayerObjectId(forValue, this, serializers, backendMetadatas);
				} else {
				}
			}
			
			
			//I can be writing a PlayerMetadatas.playerObjectId  
			if (forValue != null
					&& this.manager.isComponent(forValue.getClass())
					&& this.manager.getPlayerMetadatasWritingStack().size() > 0
					&& forValue == this.manager.getPlayerMetadatasWritingStack().peek().getPlayerObjectId()) {
				backendMetadatas.setIsComponent(true);
				backendMetadatas.setIsComponentPlayerObjectId(true);
			}
			
			try {
				this.manager.getPlayerMetadatasWritingStack().push(backendMetadatas);
				this.writeFieldName(this.manager.getConfig().getPlayerMetadatasName());
				this.writeObject(backendMetadatas);				
			} finally {
				if (this.manager.getPlayerMetadatasWritingStack() != null) {
					PlayerMetadatas backendMetadatasPoped = this.manager.getPlayerMetadatasWritingStack().pop();
					if (backendMetadatasPoped != backendMetadatas) {
						throw new RuntimeException("This should not happen");
					}					
				}
			}
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object). Injecting field \"{0}\": {1}",
						this.manager.getConfig().getPlayerMetadatasName(), backendMetadatas));
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