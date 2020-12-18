package org.jsonplayback.player.implementation;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.jsonplayback.player.IdentityRefKey;
import org.jsonplayback.player.LazyProperty;
import org.jsonplayback.player.PlayerMetadatas;
import org.jsonplayback.player.PlayerSnapshot;
import org.jsonplayback.player.SignatureBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.PropertyWriter;

public class PlayerJsonSerializer extends JsonSerializer<Object> {
	private static Logger logger = LoggerFactory.getLogger(PlayerJsonSerializer.class);
	
	//IPlayerManagerImplementor manager;
	IPlayerManagersHolderImplementor managersHolder;
	private SerializerProvider serializers;

	public PlayerJsonSerializer configManagersHolder(IPlayerManagersHolderImplementor managersHolder) {
		this.managersHolder = managersHolder;
		return this;
	}

	private ThreadLocal<Stack<Object>> currSerializationBeanStackTL = new ThreadLocal<>();
	
	public Object getCurrSerializationBean() {
		if (this.getCurrSerializationBeanStackTL().get().size() > 0) {
			return this.getCurrSerializationBeanStackTL().get().peek();
		} else {
			return null;			
		}
	}

	protected ThreadLocal<Stack<Object>> getCurrSerializationBeanStackTL() {
		if (currSerializationBeanStackTL.get() == null) {
			currSerializationBeanStackTL.set(new Stack<>());
		}
		return currSerializationBeanStackTL;
	}

	@SuppressWarnings("rawtypes")
	JsonSerializer delegate;

	public PlayerJsonSerializer(JsonSerializer<Object> delegate) {
		super();
		this.delegate = delegate;
	}

	// private Boolean isPersistentClass = null;

	private PlayerBeanPropertyWriter hbIdPropertyWriter = null;

	public PlayerBeanPropertyWriter getPropertyWritter(String propertyName)
			throws IOException, JsonProcessingException {
		if (this.delegate instanceof BeanSerializer) {
			BeanSerializer beanSerializer = (BeanSerializer) this.delegate;
			Iterator<PropertyWriter> iterator = beanSerializer.properties();
			while (iterator.hasNext()) {
				PropertyWriter propertyWriter = (PropertyWriter) iterator.next();
				if (propertyWriter instanceof PlayerBeanPropertyWriter) {
					PlayerBeanPropertyWriter playerBeanPropertyWriter = (PlayerBeanPropertyWriter) propertyWriter;
					if (playerBeanPropertyWriter.getBeanPropertyDefinition().getInternalName().equals(propertyName)) {
						return playerBeanPropertyWriter;
					}
				} else {
					throw new RuntimeException("This should not happen! " + BeanSerializer.class);
				}
			}
		} else {
			throw new RuntimeException("this.delegate is not " + BeanSerializer.class);
		}
		throw new RuntimeException("This should not happen! " + BeanSerializer.class);
	}
	
	public void findPlayerObjectId(Object value, JsonGenerator gen, SerializerProvider serializers, PlayerMetadatas playerMetadatas)
			throws IOException, JsonProcessingException {
		if (this.delegate instanceof BeanSerializer) {
			BeanSerializer beanSerializer = (BeanSerializer) this.delegate;
			Iterator<PropertyWriter> iterator = beanSerializer.properties();
			while (iterator.hasNext()) {
				PropertyWriter propertyWriter = (PropertyWriter) iterator.next();
				if (propertyWriter instanceof PlayerBeanPropertyWriter) {
					PlayerBeanPropertyWriter playerBeanPropertyWriter = (PlayerBeanPropertyWriter) propertyWriter;
					if (playerBeanPropertyWriter.getIsPlayerObjectId()) {
						try {
							if (logger.isTraceEnabled()) {
								logger.trace(
										"writePlayerObjectId(Object, JsonGenerator, SerializerProvider):\n"
												+ " playerBeanPropertyWriter.serializeAsFieldPlayerObjectIdentifier(value, gen, serializers)");
							}
							playerBeanPropertyWriter.findFieldPlayerObjectIdentifierValue(value, serializers, playerMetadatas);
						} catch (Exception e) {
							throw new RuntimeException("Nao deveria acontecer", e);
						}
						break;
					}
				}
			}
		} else {
			throw new RuntimeException("this.delegate is not " + BeanSerializer.class);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		try {
			if (this.managersHolder.thereIsStartedManager()) {
				IPlayerManagerImplementor manager = this.managersHolder.getStartedManagerImplementor();
				this.trackRegisteredComponentOwnerIfNeeded(value, serializers);
				manager.getPlayerJsonSerializerStepStack().push(this);
				this.getCurrSerializationBeanStackTL().get().push(value);
			}
			
			Class<?> valueResolvedClass = null;

			if (!this.managersHolder.thereIsStartedManager()) {
				if (logger.isTraceEnabled()) {
					logger.trace("Not Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). !this.managersHolder.thereIsStartedManager()");
				}
				this.delegate.serialize(value, gen, serializers);
			} else if (value == null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Not Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). value == null");
				}
				this.delegate.serialize(value, gen, serializers);
			} else if (value instanceof PlayerSnapshot) {
				if (logger.isTraceEnabled()) {
					logger.trace("Not Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). value instanceof PlayerSnapshot");
				}
				this.delegate.serialize(value, gen, serializers);
			} else if (value instanceof PlayerMetadatas) {
				if (logger.isTraceEnabled()) {
					logger.trace("Not Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). value instanceof PlayerMetadatas");
				}
				this.delegate.serialize(value, gen, serializers);
			} 
//			else if (value instanceof HbObjectIdForStringify) {
//				if (logger.isTraceEnabled()) {
//					logger.trace("Not Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). value instanceof HbObjectIdForStringify");
//				}
//				this.delegate.serialize(value, gen, serializers);
//			}
			else {
				boolean wasWritenByRefOrBySigne = this.mayByRefOrBySigneSerialize(value, gen, serializers);
				if (!wasWritenByRefOrBySigne) {
					if (logger.isTraceEnabled()) {
						logger.trace("Not serialize by reference or By Signature or by reference. JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). !wasWritenByRefOrBySigne");
					}
					JsonGenerator newGen = gen;
					if (!(gen instanceof PlayerJsonGeneratorDelegate)) {
//@formatter:off
					newGen = new PlayerJsonGeneratorDelegate(gen)
							.configManagerHolder(this.managersHolder)
							.configSerializers(serializers);
//@formatter:on
					}
					// nao pode ser serializado por referencia ou lazy
					this.delegate.serialize(value, newGen, serializers);
				}
			}
		} finally {
			if (this.managersHolder.thereIsStartedManager()) {
				this.getCurrSerializationBeanStackTL().get().pop();
				IPlayerManagerImplementor manager = this.managersHolder.getStartedManagerImplementor();
				if (manager.getPlayerJsonSerializerStepStack() != null) {
					PlayerJsonSerializer playerJsonSerializer = manager.getPlayerJsonSerializerStepStack().pop();
					if (playerJsonSerializer != this) {
						throw new RuntimeException("This should not happen");
					}
				}
				this.untrackRegisteredComponentIfNeeded(value, serializers);
			}
		}
	}

	private boolean mayByRefOrBySigneSerializeNoCache(Object valueToSerialize, JsonGenerator gen,
			SerializerProvider serializers) throws IOException {
		if (valueToSerialize == null) {
			throw new IllegalArgumentException("value can not be null");
		}
		IPlayerManagerImplementor manager = this.managersHolder.getStartedManagerImplementor();
		
		JavaType valueToSerializeType = serializers.getConfig()
				.introspect(serializers.getConfig().getTypeFactory().constructType(valueToSerialize.getClass()))
				.getType();
		Class<?> valueToSerializeClass = valueToSerializeType.getRawClass();
		Class<?> ownerValueClass = null;
		
		AssociationAndComponentTrackInfo aacTrackInfo = null;
		String pathFromOwnerRoot = null;
		if(!manager.isPersistentClass(valueToSerializeClass)) {
			aacTrackInfo = manager.getCurrentAssociationAndComponentTrackInfo();
			if (aacTrackInfo != null) {
				pathFromOwnerRoot = aacTrackInfo.getEntityAndComponentPath().getAacKey().getPathFromOwner();
				ownerValueClass = aacTrackInfo.getEntityAndComponentPath().getAacKey().getEntityClassRootOwner();
			}
		}
		
		// boolean wasWritenByLazyRef = this.mayWriteBySignatureRef(owner,
		// valueToSerialize, gen, serializers, fieldName);
		//boolean wasWritenByLazyRef = this.mayWriteBySignatureRef(valueToSerialize, gen, serializers);
		//if (wasWritenByLazyRef) {
		//	return true;
		//} else {
			if (manager.getIdByObjectMap().containsKey(new IdentityRefKey(valueToSerialize))) {
				gen.writeStartObject(); 
				PlayerMetadatas playerMetadatas = manager.getConfig().getMetadataInstantiator().apply(manager);
				playerMetadatas.setIdRef(manager.getIdByObjectMap().get(new IdentityRefKey(valueToSerialize)));
				if (logger.isTraceEnabled()) {
					logger.trace(
						MessageFormat.format(
							"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object).\n"
							+ " gen.writeStartObject();\n"
							+ " gen.writeEndObject();\n"
							+ " gen.writeFieldName(\"{0}\");\n"
							+ " gen.writeObject({1});\n",								
						manager.getConfig().getPlayerMetadatasName(), 
						playerMetadatas));
				}
				
				try {
					manager.getPlayerMetadatasWritingStack().push(playerMetadatas);
					gen.writeFieldName(manager.getConfig().getPlayerMetadatasName());
					gen.writeObject(playerMetadatas);
//					manager.getMetadatasCacheMap().put(new IdentityRefKey(valueToSerialize), playerMetadatas);
				} finally {
					if (manager.getPlayerMetadatasWritingStack() != null) {
						PlayerMetadatas playerMetadatasPoped = manager.getPlayerMetadatasWritingStack().pop();
						if (playerMetadatasPoped != playerMetadatas) {
							throw new RuntimeException("This should not happen");
						}					
					}
				}
				
				gen.writeEndObject();
				return true;
//			}
//			else if (valueToSerialize instanceof HibernateProxy) {
//				Class<?> forValueClass = valueToSerialize.getClass().getSuperclass();
//				if (manager.getIdByObjectMap().containsKey(new IdentityRefKey(valueToSerialize))) {
//					gen.writeStartObject();
//					PlayerMetadatas playerMetadatas = new PlayerMetadatas();
//					playerMetadatas.setIdRef(manager.getIdByObjectMap().get(new IdentityRefKey(valueToSerialize)));
//					if (logger.isTraceEnabled()) {
//						logger.trace(
//							MessageFormat.format(
//								"Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). Writing\n"
//								+ " gen.writeStartObject();\n"
//								+ " gen.writeFieldName(\"{0}\");\n"
//								+ " gen.writeObject({1});\n"
//								+ " gen.writeEndObject();",								
//							manager.getConfig().getPlayerMetadatasName(), 
//							playerMetadatas));
//					}
//					try {
//						manager.getPlayerMetadatasWritingStack().push(playerMetadatas);				
//						gen.writeFieldName(manager.getConfig().getPlayerMetadatasName());
//						gen.writeObject(playerMetadatas);
////						manager.getMetadatasCacheMap().put(new IdentityRefKey(valueToSerialize), playerMetadatas);
//					} finally {
//						if (manager.getPlayerMetadatasWritingStack() != null) {
//							PlayerMetadatas playerMetadatasPoped = manager.getPlayerMetadatasWritingStack().pop();
//							if (playerMetadatasPoped != playerMetadatas) {
//								throw new RuntimeException("This should not happen");
//							}					
//						}
//					}
//					gen.writeEndObject();
//					return true;
//				} else {
//					return false;
//				}
//			//} else if (manager.getObjPersistenceSupport().isPersistentCollection(aacTrackInfo, valueToSerialize)) {
			} else if (this.mayWriteBySignatureRef(valueToSerialize, gen, serializers)) {
				return true;
			} else if (manager.getObjPersistenceSupport().isCollectionRelationship(ownerValueClass, pathFromOwnerRoot)) {
				return false;
			} else {
				return false;
			}
		//}
	}

	private boolean mayByRefOrBySigneSerialize(Object valueToSerialize, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		JavaType valueToSerializeType = serializers.getConfig()
				.introspect(serializers.getConfig().getTypeFactory().constructType(valueToSerialize.getClass()))
				.getType();
		Class<?> forValueClass = valueToSerializeType.getRawClass();
		PlayerMetadatas playerMetadatas = this.managersHolder.getStartedManagerImplementor().getMetadatasCacheMap().get(new IdentityRefKey(valueToSerialize));
		IPlayerManagerImplementor manager = this.managersHolder.getStartedManagerImplementor();
		
		if (playerMetadatas != null) {
			if (playerMetadatas.getId() != null) {
				long idRef = playerMetadatas.getId(); 
				playerMetadatas = manager.getConfig().getMetadataInstantiator().apply(manager);
				playerMetadatas.setIdRef(idRef);
//			} else if (playerMetadatas.getIdRef() != null) {
//				long idRef = playerMetadatas.getIdRef();
//				playerMetadatas = new PlayerMetadatas();
//				playerMetadatas.setIdRef(idRef);
			} else {
				throw new RuntimeException("This should not happen");
			}
			if (logger.isTraceEnabled()) {
				logger.trace(
					MessageFormat.format(
						"mayWriteBySignatureRef(). Metadatas cache found. wrinting:\n"
						+ " gen.writeStartObject();\n"
						+ " gen.writeFieldName(\"{0}\");\n"
						+ " gen.writeObject({1});\n"
						+ " gen.writeEndObject();",								
					manager.getConfig().getPlayerMetadatasName(), 
					playerMetadatas));
			}			
			
			gen.writeStartObject();
			try {
				manager.getPlayerMetadatasWritingStack().push(playerMetadatas);				
				gen.writeFieldName(manager.getConfig().getPlayerMetadatasName());
				gen.writeObject(playerMetadatas);
			} finally {
				if (manager.getPlayerMetadatasWritingStack() != null) {
					PlayerMetadatas playerMetadatasPoped = manager.getPlayerMetadatasWritingStack().pop();
					if (playerMetadatasPoped != playerMetadatas) {
						throw new RuntimeException("This should not happen");
					}					
				}
			}					
			gen.writeEndObject();
			
			if (logger.isTraceEnabled()) {
				logger.trace(
					MessageFormat.format(
						"mayWriteBySignatureRef(). Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). wrinting:\n"
						+ " gen.writeStartObject();\n"
						+ " gen.writeFieldName(\"{0}\");\n"
						+ " gen.writeObject({1});\n"
						+ " gen.writeEndObject();",								
					manager.getConfig().getPlayerMetadatasName(), 
					playerMetadatas));
			}
			
			return true;
		} else {
			return this.mayByRefOrBySigneSerializeNoCache(valueToSerialize, gen, serializers);			
		}
	}
	
	private boolean mayWriteBySignatureRef(Object valueToSerialize, JsonGenerator gen, SerializerProvider serializers)
			throws IOException {
		IPlayerManagerImplementor manager = this.managersHolder.getStartedManagerImplementor(); 
		JavaType valueToSerializeType = serializers.getConfig()
				.introspect(serializers.getConfig().getTypeFactory().constructType(valueToSerialize.getClass()))
				.getType();
		Class<?> valueToSerializeClass = valueToSerializeType.getRawClass();
		Class<?> unwrappedValueToSerializeClass = manager.getConfig().getObjPersistenceSupport().unwrappRealType(valueToSerializeClass);
		Class<?> ownerValueClass = null;

		PlayerBeanPropertyWriter currPropertyWriter = null;

		if (manager.getPlayerBeanPropertyWriterStepStack().size() > 0) {
			currPropertyWriter = manager.getPlayerBeanPropertyWriterStepStack().peek();
		}

		AssociationAndComponentTrackInfo aacTrackInfo = null;
		String pathFromOwnerRoot = null;
		if(!manager.isPersistentClass(valueToSerializeClass)) {
			aacTrackInfo = manager.getCurrentAssociationAndComponentTrackInfo();
			if (aacTrackInfo != null) {
				pathFromOwnerRoot = aacTrackInfo.getEntityAndComponentPath().getAacKey().getPathFromOwner();
				ownerValueClass = aacTrackInfo.getEntityAndComponentPath().getAacKey().getEntityClassRootOwner();
			}
		}

		if (aacTrackInfo == null && currPropertyWriter == null) {
			return false;
		} else if (manager.isPersistentClass(valueToSerializeClass)
				&& currPropertyWriter != null
				&& !manager.getConfig().getNonLazybleClasses().contains(unwrappedValueToSerializeClass)) {
//			Class<?> forValueClass = valueToSerialize.getClass().getSuperclass();
			if ((manager.isCurrentPathFromLastEntityAnEntityRelationship() && manager.getConfig().isSerialiseBySignatureAllRelationship())
			//if ((manager.getConfig().isSerialiseBySignatureAllRelationship())
					|| manager.getConfig().getObjPersistenceSupport().isLazyUnitialized(valueToSerialize)) {
				gen.writeStartObject();
				manager.currIdPlusPlus();
				manager.getObjectByIdMap().put(manager.getCurrId(), valueToSerialize);
				manager.getIdByObjectMap().put(new IdentityRefKey(valueToSerialize),
						manager.getCurrId());
				
				PlayerMetadatas playerMetadatas = manager.getConfig().getMetadataInstantiator().apply(manager);
				
				playerMetadatas.setId(manager.getCurrId());
				playerMetadatas.setIsLazyUninitialized(true);
				playerMetadatas.setIsAssociative(true);
				
				if (logger.isTraceEnabled()) {
					logger.trace(MessageFormat.format(
							"mayWriteBySignatureRef(). Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider):\n"
									+ " gen.writeStartObject();\n"
									+ " {0}: {1};\n"
									+ " {2}: {3}\n"
									+ " {4}: {5}",
							"playerMetadatas.id", manager.getCurrId(),
							"playerMetadatas.isLazyUninitialized", true,
							"playerMetadatas.isAssociative", true));
				}
				
//				if (!manager.isNeverSigned(valueToSerializeClass)) {
					SignatureBean signatureBean = manager
							//.generateLazySignature((HibernateProxy) valueToSerialize);
							.generateSignature(valueToSerialize);
					String signatureStr = manager.serializeSignature(signatureBean);
					playerMetadatas.setSignature(signatureStr);
					
					if (logger.isTraceEnabled()) {
						logger.trace(MessageFormat.format(
								"mayWriteBySignatureRefNoCache(). Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider):\n"
												+ " this.findPlayerObjectId(valueToSerialize, gen, serializers, {0});",
												playerMetadatas));
					}
					
					this.findPlayerObjectId(valueToSerialize, gen, serializers, playerMetadatas);
//				}
				
				if (logger.isTraceEnabled()) {
					logger.trace(
						MessageFormat.format(
							"mayWriteBySignatureRef(). Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider):\n"
							+ " gen.writeFieldName(\"{0}\");\n"
							+ " gen.writeObject({1});\n"
							+ " gen.writeEndObject();",								
						manager.getConfig().getPlayerMetadatasName(), 
						playerMetadatas));
				}
				try {
					manager.getPlayerMetadatasWritingStack().push(playerMetadatas);				
					gen.writeFieldName(manager.getConfig().getPlayerMetadatasName());
					gen.writeObject(playerMetadatas);
					manager.getMetadatasCacheMap().put(new IdentityRefKey(valueToSerialize), playerMetadatas);
				} finally {
					if (manager.getPlayerMetadatasWritingStack() != null) {
						PlayerMetadatas playerMetadatasPoped = manager.getPlayerMetadatasWritingStack().pop();
						if (playerMetadatasPoped != playerMetadatas) {
							throw new RuntimeException("This should not happen");
						}					
					}
				}
				gen.writeEndObject();
				return true;
			} else {
				if (manager.getIdByObjectMap().containsKey(new IdentityRefKey(valueToSerialize))) {
					gen.writeStartObject();
					PlayerMetadatas playerMetadatasOld = manager.getMetadatasCacheMap().get(new IdentityRefKey(valueToSerialize));
					 
					PlayerMetadatas playerMetadatas = manager.getConfig().getMetadataInstantiator().apply(manager);
					playerMetadatas.setIdRef(manager.getIdByObjectMap().get(new IdentityRefKey(valueToSerialize)));
					try {
						manager.getPlayerMetadatasWritingStack().push(playerMetadatas);				
						gen.writeFieldName(manager.getConfig().getPlayerMetadatasName());
						gen.writeObject(playerMetadatas);
						manager.getMetadatasCacheMap().put(new IdentityRefKey(valueToSerialize), playerMetadatas);
					} finally {
						if (manager.getPlayerMetadatasWritingStack() != null) {
							PlayerMetadatas playerMetadatasPoped = manager.getPlayerMetadatasWritingStack().pop();
							if (playerMetadatasPoped != playerMetadatas) {
								throw new RuntimeException("This should not happen");
							}					
						}
					}					
					gen.writeEndObject();
					if (logger.isTraceEnabled()) {
						logger.trace(
							MessageFormat.format(
								"mayWriteBySignatureRef(). Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). wrinting:\n"
								+ " gen.writeStartObject();\n"
								+ " gen.writeFieldName(\"{0}\");\n"
								+ " gen.writeObject({1});\n"
								+ " gen.writeEndObject();",								
							manager.getConfig().getPlayerMetadatasName(), 
							playerMetadatas));
					}
					return true;
				} else {
					return false;
				}
			}
		//} else if (manager.getObjPersistenceSupport().isPersistentCollection(aacTrackInfo, valueToSerialize)) {
		} else if (manager.getObjPersistenceSupport().isCollectionRelationship(ownerValueClass, pathFromOwnerRoot)) {
			//PersistentCollection pcvalue = (PersistentCollection) valueToSerialize;
			//if (manager.getConfig().isSerialiseBySignatureAllRelationship() || !pcvalue.wasInitialized()) {
			if (manager.getConfig().isSerialiseBySignatureAllRelationship()
					|| manager.getObjPersistenceSupport().isLazyUnitialized(
							valueToSerialize, 
							aacTrackInfo.getEntityOwner(), 
							pathFromOwnerRoot)
					) {
				gen.writeStartObject();
				PlayerMetadatas playerMetadatas = manager.getConfig().getMetadataInstantiator().apply(manager);				
				playerMetadatas.setIsLazyUninitialized(true);
				playerMetadatas.setIsAssociative(true);
				SignatureBean signatureBean = manager.generateLazySignature((Collection<?>) valueToSerialize);
				String signatureStr = manager.serializeSignature(signatureBean);
				playerMetadatas.setSignature(signatureStr);
				try {
					manager.getPlayerMetadatasWritingStack().push(playerMetadatas);				
					gen.writeFieldName(manager.getConfig().getPlayerMetadatasName());
					gen.writeObject(playerMetadatas);				
					manager.getMetadatasCacheMap().put(new IdentityRefKey(valueToSerialize), playerMetadatas);
				} finally {
					if (manager.getPlayerMetadatasWritingStack() != null) {
						PlayerMetadatas playerMetadatasPoped = manager.getPlayerMetadatasWritingStack().pop();
						if (playerMetadatasPoped != playerMetadatas) {
							throw new RuntimeException("This should not happen");
						}					
					}
				}
				gen.writeEndObject();
				
				if (logger.isTraceEnabled()) {
					logger.trace(
						MessageFormat.format(
							"mayWriteBySignatureRef(). Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider).\n"
							+ " gen.writeStartObject();\n"
							+ " gen.writeFieldName(\"{0}\");\n"
							+ " gen.writeObject({1});\n"
							+ " gen.writeEndObject();",								
						manager.getConfig().getPlayerMetadatasName(), 
						playerMetadatas));
				}
				return true;
			} else {
				return false;
			}
		} else {
			if ((manager.isCurrentPathFromLastEntityAnEntityRelationship() && manager.getConfig().isSerialiseBySignatureAllRelationship())
			//if ((manager.getConfig().isSerialiseBySignatureAllRelationship())
					&& currPropertyWriter != null
					&& aacTrackInfo != null
					) {
					//&& currPropertyWriter.getRelationshipOwnerClass() != null) {
				//if (entityAndComponentTrackInfo != null) {
				if (currPropertyWriter.getIsPersistent()) {
					gen.writeStartObject();
					
					manager.currIdPlusPlus();
					manager.getObjectByIdMap().put(manager.getCurrId(), valueToSerialize);
					manager.getIdByObjectMap().put(new IdentityRefKey(valueToSerialize),
							manager.getCurrId()); 
					PlayerMetadatas playerMetadatas = manager.getConfig().getMetadataInstantiator().apply(manager);
					playerMetadatas.setId(manager.getCurrId());
					playerMetadatas.setIsLazyUninitialized(true);
					playerMetadatas.setIsAssociative(true);

					SignatureBean signatureBean = null;
					if (manager.getObjPersistenceSupport().isComponentByTrack(aacTrackInfo)) {
						signatureBean = manager.generateLazySignatureForCollRelashionship(
							currPropertyWriter.getCurrOwner().getClass(),
							currPropertyWriter.getBeanPropertyDefinition().getInternalName(),
							currPropertyWriter.getCurrOwner(),
							valueToSerialize);					
					} else if (manager.isPersistentClass(aacTrackInfo.getEntityOwner().getClass())) {
						signatureBean = manager.generateSignature(valueToSerialize);	
					} else {
						throw new RuntimeException("This should not happen");
					}
					
					String signatureStr = manager.serializeSignature(signatureBean);
					playerMetadatas.setSignature(signatureStr);

					if (logger.isTraceEnabled()) {
						logger.trace(MessageFormat.format(
								"mayWriteBySignatureRef(). Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider):\n"
										+ " gen.writeStartObject();\n"
										+ " playerMetadatas.isLazyUninitialized: {0};\n"
										+ " playerMetadatas.isAssociative: {1});\n"
										+ " playerMetadatas.signature: \"{2}\";\n",
								true,
								true,
								signatureStr));
					}
					
					if (!(valueToSerialize instanceof Collection)) {
						if (logger.isTraceEnabled()) {
							logger.trace(
									"mayWriteBySignatureRefNoCache(). Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider). !(valueToSerialize instanceof Collection):\n"
											+ " this.findPlayerObjectId(valueToSerialize, gen, serializers, playerMetadatas)");
						}
						this.findPlayerObjectId(valueToSerialize, gen, serializers, playerMetadatas);
					}
					if (logger.isTraceEnabled()) {
						logger.trace(
							MessageFormat.format(
								"Intercepting com.fasterxml.jackson.core.JsonGenerator.writeStartObject(Object).\n"
								+ " gen.writeFieldName(\"{0}\");\n"
								+ " gen.writeObject({1});\n"
								+ " gen.writeEndObject();",								
							manager.getConfig().getPlayerMetadatasName(), 
							playerMetadatas));
					}
					try {
						manager.getPlayerMetadatasWritingStack().push(playerMetadatas);				
						gen.writeFieldName(manager.getConfig().getPlayerMetadatasName());
						gen.writeObject(playerMetadatas);
						manager.getMetadatasCacheMap().put(new IdentityRefKey(valueToSerialize), playerMetadatas);
					} finally {
						if (manager.getPlayerMetadatasWritingStack() != null) {
							PlayerMetadatas playerMetadatasPoped = manager.getPlayerMetadatasWritingStack().pop();
							if (playerMetadatasPoped != playerMetadatas) {
								throw new RuntimeException("This should not happen");
							}					
						}
					}
					gen.writeEndObject();
					return true;
				}
			} else if (!manager.getConfig().isIgnoreAllLazyProperty()
					&& currPropertyWriter != null) {
				LazyProperty lazyProperty = currPropertyWriter.getAnnotation(LazyProperty.class);
				if (lazyProperty != null) {
					try {
						long size = 0;
						if (valueToSerialize instanceof byte[]) {
							byte[] valueToSerializeByteArr = (byte[]) valueToSerialize;
							size = valueToSerializeByteArr.length;
						} else if (valueToSerialize instanceof Blob) {
							Blob valueToSerializeBlob = (Blob) valueToSerialize;
							size = valueToSerializeBlob.length();
						} else if (valueToSerialize instanceof String) {
							String valueToSerializeStr = (String) valueToSerialize;
							size = valueToSerializeStr.length();
						} else if (valueToSerialize instanceof Clob) {
							Clob valueToSerializeClob = (Clob) valueToSerialize;
							size = valueToSerializeClob.length();
						} else {
							throw new RuntimeException(
									"Property type does not support LazyProperty: " + currPropertyWriter);
						}

						if (lazyProperty.nonLazyMaxSize() > 0 && size < lazyProperty.nonLazyMaxSize()) {
							return false;
						} else {
							gen.writeStartObject(); 
							PlayerMetadatas playerMetadatas = manager.getConfig().getMetadataInstantiator().apply(manager);
							playerMetadatas.setIsLazyUninitialized(true);
							playerMetadatas.setIsLazyProperty(true);
							SignatureBean signatureBean = manager.generateLazySignatureForLazyProperty(
									currPropertyWriter.getCurrOwner().getClass(),
									currPropertyWriter.getBeanPropertyDefinition().getInternalName(),
									currPropertyWriter.getCurrOwner(),
									valueToSerialize);
							String signatureStr = manager.serializeSignature(signatureBean);
							playerMetadatas.setSignature(signatureStr);
							try {
								manager.getPlayerMetadatasWritingStack().push(playerMetadatas);
								gen.writeFieldName(manager.getConfig().getPlayerMetadatasName());
								gen.writeObject(playerMetadatas);
								manager.getMetadatasCacheMap().put(new IdentityRefKey(valueToSerialize),
										playerMetadatas);
							} finally {
								if (manager.getPlayerMetadatasWritingStack() != null) {
									PlayerMetadatas playerMetadatasPoped = manager
											.getPlayerMetadatasWritingStack().pop();
									if (playerMetadatasPoped != playerMetadatas) {
										throw new RuntimeException("This should not happen");
									}
								}
							}
							gen.writeEndObject();
							if (logger.isTraceEnabled()) {
								logger.trace(MessageFormat.format(
										"mayWriteBySignatureRef(). Found LazyProperty. Intercepting JsonSerializer.serialize(T, JsonGenerator, SerializerProvider).\n"
												+ " gen.writeStartObject();\n" + " gen.writeFieldName(\"{0}\");\n"
												+ " gen.writeObject({1});\n" + " gen.writeEndObject();",
										manager.getConfig().getPlayerMetadatasName(), playerMetadatas));
							}
							return true;

						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			return false;
		}
	}

	protected void trackRegisteredComponentOwnerIfNeeded(Object instance, SerializerProvider serializers) throws JsonProcessingException, IOException {
		IPlayerManagerImplementor manager = this.managersHolder.getStartedManagerImplementor();
		List<OwnerAndProperty> ownerPath = manager.getRegisteredComponentOwnerList(instance);
		if (ownerPath.size() > 0) {
			for (OwnerAndProperty ownerAndProperty : ownerPath) {
				JsonSerializer<?> jsonSerializer = serializers.findValueSerializer(ownerAndProperty.getOwner().getClass());
				if (jsonSerializer instanceof PlayerJsonSerializer) {
					PlayerJsonSerializer playerJsonSerializer = (PlayerJsonSerializer) jsonSerializer;
					PlayerBeanPropertyWriter playerBeanPropertyWriter = playerJsonSerializer.getPropertyWritter(ownerAndProperty.getProperty());
					
					playerJsonSerializer.getCurrSerializationBeanStackTL().get().push(ownerAndProperty.getOwner());
					playerBeanPropertyWriter.getCurrOwnerStackTL().get().push(ownerAndProperty.getOwner());
					manager.getPlayerJsonSerializerStepStack().push(playerJsonSerializer);
					manager.getPlayerBeanPropertyWriterStepStack().push(playerBeanPropertyWriter);
					if (manager.isPersistentClass(ownerAndProperty.getOwner().getClass())) {
						Object hbId = manager.getPlayerObjectId(ownerAndProperty.getOwner()); 
						PlayerMetadatas dammyMetadatas = manager.getConfig().getMetadataInstantiator().apply(manager);
						dammyMetadatas.setPlayerObjectId(hbId);
						manager.getPlayerMetadatasWritingStack().push(dammyMetadatas);
					}
				} else {
					throw new RuntimeException("This should not happen!");
				}
			}
		};
	}
	
	protected void untrackRegisteredComponentIfNeeded(Object instance, SerializerProvider serializers) throws JsonProcessingException, IOException {
		IPlayerManagerImplementor manager = this.managersHolder.getStartedManagerImplementor();
		List<OwnerAndProperty> ownerPath = manager.getRegisteredComponentOwnerList(instance);
		List<OwnerAndProperty> ownerReversedPath = new ArrayList<>(ownerPath);
		Collections.reverse(ownerReversedPath);
		
		if (ownerPath.size() > 0) {
			for (OwnerAndProperty ownerAndProperty : ownerReversedPath) {
				JsonSerializer<?> jsonSerializer = serializers.findValueSerializer(ownerAndProperty.getOwner().getClass());
				if (jsonSerializer instanceof PlayerJsonSerializer) {
					PlayerJsonSerializer playerJsonSerializer = (PlayerJsonSerializer) jsonSerializer;
					PlayerBeanPropertyWriter playerBeanPropertyWriter = playerJsonSerializer.getPropertyWritter(ownerAndProperty.getProperty());
					
					Object owner = playerJsonSerializer.getCurrSerializationBeanStackTL().get().pop();
					if (ownerAndProperty.getOwner() != owner) {
						throw new RuntimeException("This should not happen!");
					}
					owner = playerBeanPropertyWriter.getCurrOwnerStackTL().get().pop();
					if (ownerAndProperty.getOwner() != owner) {
						throw new RuntimeException("This should not happen!");
					}
					PlayerJsonSerializer poppedPlayerJsonSerializer = manager.getPlayerJsonSerializerStepStack().pop();
					if (poppedPlayerJsonSerializer != playerJsonSerializer) {
						throw new RuntimeException("This should not happen!");
					}	
					PlayerBeanPropertyWriter poppedPlayerBeanPropertyWriter = manager.getPlayerBeanPropertyWriterStepStack().pop();
					if (poppedPlayerBeanPropertyWriter != playerBeanPropertyWriter) {
						throw new RuntimeException("This should not happen!");
					}
					if (manager.isPersistentClass(ownerAndProperty.getOwner().getClass())) {
						Object hbId = manager.getPlayerObjectId(ownerAndProperty.getOwner());
						PlayerMetadatas dammyMetadatas = manager.getPlayerMetadatasWritingStack().pop();
						if (hbId != dammyMetadatas.getPlayerObjectId()) {
							throw new RuntimeException("This should not happen!");	
						}
					}
				} else {
					throw new RuntimeException("This should not happen!");
				}
			}
		};
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
	
	@Override
	public String toString() {
		return "PlayerJsonSerializer for " + this.delegate;
	}
	
	protected String mountPathFromStack(String[] pathStack) {
		return this.mountPathFromStack(Arrays.asList(pathStack));
	}
	
	protected String mountPathFromStack(Collection<String> pathStack) {
		String pathResult = "";
		String dotStr = "";
		for (String pathItem : pathStack) {
			pathResult += dotStr + pathItem;
			dotStr = ".";
		}
		return pathResult;
	}
}
