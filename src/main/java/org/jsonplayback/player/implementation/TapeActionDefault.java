package org.jsonplayback.player.implementation;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.proxy.HibernateProxy;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.SignatureBean;
import org.jsonplayback.player.TapeAction;
import org.jsonplayback.player.TapeActionType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

public class TapeActionDefault extends TapeAction {
	private Object resolvedOwnerValue = null;
	private boolean isResolvedSettedValue = false;
	private Object resolvedSettedValue = null;
	private Object simpleSettedValue = null;
	@SuppressWarnings("rawtypes")
	private Collection resolvedColletion = null;
	private BeanDeserializerBase resolvedBeanDeserializer = null;
	@SuppressWarnings("rawtypes")
	private Class resolvedOwnerPlayerType = null;
	private String resolvedJavaPropertyName = null;
	private boolean isResolvedJavaPropertyName = false;
	
	/**
	 * Cache!
	 */
	protected DefaultDeserializationContext defaultDeserializationContext;
	
	public Object getResolvedOwnerValue() {
		return resolvedOwnerValue;
	}

	public void setResolvedOwnerValue(Object resolvedOwnerValue) {
		this.resolvedOwnerValue = resolvedOwnerValue;
	}

	public boolean isResolvedSettedValue() {
		return isResolvedSettedValue;
	}

	public void setResolvedSettedValue(boolean isResolvedSettedValue) {
		this.isResolvedSettedValue = isResolvedSettedValue;
	}

	public Object getResolvedSettedValue() {
		return resolvedSettedValue;
	}

	public void setResolvedSettedValue(Object resolvedSettedValue) {
		this.resolvedSettedValue = resolvedSettedValue;
	}

	public Collection getResolvedColletion() {
		return resolvedColletion;
	}

	public void setResolvedColletion(Collection resolvedColletion) {
		this.resolvedColletion = resolvedColletion;
	}

	public BeanDeserializerBase getResolvedBeanDeserializer() {
		return resolvedBeanDeserializer;
	}

	public void setResolvedBeanDeserializer(BeanDeserializerBase resolvedBeanDeserializer) {
		this.resolvedBeanDeserializer = resolvedBeanDeserializer;
	}

	public Class getResolvedOwnerPlayerType() {
		return resolvedOwnerPlayerType;
	}

	public void setResolvedOwnerPlayerType(Class resolvedOwnerPlayerType) {
		this.resolvedOwnerPlayerType = resolvedOwnerPlayerType;
	}

	public String getResolvedJavaPropertyName() {
		return resolvedJavaPropertyName;
	}

	public void setResolvedJavaPropertyName(String resolvedJavaPropertyName) {
		this.resolvedJavaPropertyName = resolvedJavaPropertyName;
	}

	public boolean isResolvedJavaPropertyName() {
		return isResolvedJavaPropertyName;
	}

	public void setResolvedJavaPropertyName(boolean isResolvedJavaPropertyName) {
		this.isResolvedJavaPropertyName = isResolvedJavaPropertyName;
	}

	public Object getSimpleSettedValue() {
		return simpleSettedValue;
	}

	public DefaultDeserializationContext getDefaultDeserializationContext() {
		return defaultDeserializationContext;
	}

	public void setDefaultDeserializationContext(DefaultDeserializationContext defaultDeserializationContext) {
		this.defaultDeserializationContext = defaultDeserializationContext;
	}
	
	protected String resolveJavaPropertyName(ObjectMapper objectMapper, IPlayerManager manager,
			HashMap<Long, Object> creationRefMap) {
		if (!this.isResolvedJavaPropertyName) {
			this.isResolvedJavaPropertyName = true;
			if (this.getFieldName() != null) {
				SettableBeanProperty settableBeanProperty = this
						.resolveBeanDeserializer(objectMapper, manager, creationRefMap)
						.findProperty(this.getFieldName());
				if (settableBeanProperty == null) {
					throw new RuntimeException(MessageFormat.format("O field nao existe na entidade:\n{0}", this));
				}
				this.resolvedJavaPropertyName = settableBeanProperty.getName();
			}
		}
		return resolvedJavaPropertyName;
	}

	/**
	 * Somente deve ser chamado a partir de IReplayable.preProcessPlayBack.
	 * 
	 * @param manager
	 * @param creationRefMap
	 * @return
	 */
	protected Object resolveOwnerValue(IPlayerManager manager, HashMap<Long, Object> creationRefMap) {
		if (this.resolvedOwnerValue == null) {
			if (this.getOwnerSignatureStr() != null) {
				SignatureBean signatureBean = manager.deserializeSignature(this.getOwnerSignatureStr());
				this.resolvedOwnerValue = manager.getBySignature(signatureBean);
			} else if (this.getOwnerCreationId() != null) {
				try {
					this.resolvedOwnerValue = this.resolveOwnerPlayerType(manager, creationRefMap).newInstance();
					
					creationRefMap.put(this.getOwnerCreationId(), this.resolvedOwnerValue);
				} catch (Exception e) {
					throw new RuntimeException(MessageFormat.format("This should not happen\naction:\n{0}", this),
							e);
				}
			} else if (this.getOwnerCreationRefId() != null) {
				this.resolvedOwnerValue = creationRefMap.get(this.getOwnerCreationRefId());
			} else {
				throw new RuntimeException(MessageFormat.format("This should not happen\naction:\n{0}", this));
			}
		}
		return this.resolvedOwnerValue;
	}

	protected Object resolveSettedValue(ObjectMapper objectMapper, IPlayerManager manager,
			HashMap<Long, Object> creationRefMap) {
		if (!this.isResolvedSettedValue) {
			this.isResolvedSettedValue = true;
			if (this.getSettedCreationRefId() != null) {
				if (creationRefMap == null) {
					//nada: estah sendo chamado a partir da deserializacao Jaxrs/Jacson
				} else {
					//obtendo do creationRefMap: estah sendo chamado a partir do IReplayable
					this.resolvedSettedValue = creationRefMap.get(this.getSettedCreationRefId());					
				}
			} else if (this.getSettedSignatureStr() != null) {
				SignatureBean signatureBean = manager.deserializeSignature(this.getSettedSignatureStr());
				this.resolvedSettedValue = manager.getBySignature(signatureBean);
			} else if (this.getFieldName() != null) {
				// Tipo nao bean
				try {
					Map<String, Object> dummyMap = new LinkedHashMap<>();
					dummyMap.put("dummyPrp", this.simpleSettedValue);
					StringWriter writer = new StringWriter();
					objectMapper.writeValue(writer, dummyMap);
					JsonParser jsonParser = objectMapper.getFactory().createParser(writer.toString());
					//begin object
					if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
						throw new RuntimeException("This should not happen!!!!");
					}
					//dummyPrp 
					if (jsonParser.nextToken() != JsonToken.FIELD_NAME) {
						throw new RuntimeException("This should not happen!!!!");
					}
					String dummyPrpName = jsonParser.getCurrentName();
					if (!"dummyPrp".equals(dummyPrpName)) {
						throw new RuntimeException("This should not happen!!!!");
					}
					SettableBeanProperty settableBeanProperty = this.resolveBeanDeserializer(objectMapper, manager, creationRefMap).findProperty(this.getFieldName());
					JsonDeserializer<Object> jsonDeserializer = settableBeanProperty.getValueDeserializer();
					//valor simple
					jsonParser.nextToken();
					this.resolvedSettedValue = jsonDeserializer.deserialize(jsonParser, this.resolveDefaultDeserializationContext(objectMapper, manager));
					//end object
					if (jsonParser.nextToken() != JsonToken.END_OBJECT) {
						throw new RuntimeException("This should not happen!!!!");
					}
					if (jsonParser.nextToken() != null) {
						throw new RuntimeException("This should not happen!!!!");
					}
				} catch (IOException e) {
					throw new RuntimeException(MessageFormat.format("This should not happen\naction:\n{0}", this),
							e);
				}
			}
		}
		return this.resolvedSettedValue;
	}

	@SuppressWarnings("rawtypes")
	protected Collection resolveColletion(ObjectMapper objectMapper, IPlayerManager manager,
			HashMap<Long, Object> creationRefMap) {
		if (this.resolvedColletion == null && (this.getActionType() == TapeActionType.COLLECTION_ADD || this.getActionType() == TapeActionType.COLLECTION_REMOVE)) {
			try {
				this.resolvedColletion = (Collection) PropertyUtils.getProperty(this.resolveOwnerValue(manager, creationRefMap),
						this.resolveJavaPropertyName(objectMapper, manager, creationRefMap));
			} catch (Exception e) {
				throw new RuntimeException(MessageFormat.format("This should not happen. action:\n{0}", this), e);
			}
		}
		return this.resolvedColletion;
	}

	protected DefaultDeserializationContext resolveDefaultDeserializationContext(ObjectMapper objectMapper, IPlayerManager manager) {
		if (((TapeDefault)this.getTapeOwner()).getDefaultDeserializationContext() == null) {
			((TapeDefault)this.getTapeOwner()).setDefaultDeserializationContext(((DefaultDeserializationContext) objectMapper
					.getDeserializationContext()).createInstance(objectMapper.getDeserializationConfig(), null,
							objectMapper.getInjectableValues()));
		}
		return ((TapeDefault)this.getTapeOwner()).defaultDeserializationContext;
	}
	
	protected BeanDeserializerBase resolveBeanDeserializer(ObjectMapper objectMapper, IPlayerManager manager,
			HashMap<Long, Object> creationRefMap) {
		if (this.resolvedBeanDeserializer == null) {
			JavaType entJavaType = objectMapper.getTypeFactory().constructType(this.resolveOwnerPlayerType(manager, creationRefMap));
			try {
				
				this.resolvedBeanDeserializer = (BeanDeserializerBase) this.resolveDefaultDeserializationContext(objectMapper, manager)
						.findRootValueDeserializer(entJavaType);
//				this.resolvedBeanDeserializer = (BeanDeserializerBase) objectMapper.getDeserializationContext()
//						.findRootValueDeserializer(entJavaType);
			} catch (JsonMappingException e) {
				throw new RuntimeException(MessageFormat.format("This should not happen\naction:\n{0}", this), e);
			}
		}
		return resolvedBeanDeserializer;
	}

	@SuppressWarnings("rawtypes")
	protected Class resolveOwnerPlayerType(IPlayerManager manager,
			HashMap<Long, Object> creationRefMap) {
		if (this.resolvedOwnerPlayerType == null) {
			try {
				if (this.getOwnerPlayerType() != null) {
					this.resolvedOwnerPlayerType = Class.forName(this.getOwnerPlayerType());
				} else if (this.getOwnerSignatureStr() != null) {
					this.resolveOwnerValue(manager, creationRefMap);
					if (this.resolvedOwnerValue instanceof HibernateProxy) {
						this.resolvedOwnerPlayerType = this.resolvedOwnerValue.getClass().getSuperclass();						
					} else {
						this.resolvedOwnerPlayerType = this.resolvedOwnerValue.getClass();
					}
				} else if (this.getOwnerCreationRefId() != null) {
					if (!creationRefMap.containsKey(this.getOwnerCreationRefId())) {
						throw new RuntimeException(MessageFormat.format("There is no ''{0}'' action with this creation id\naction:\n{1}", TapeActionType.CREATE , this));
					}
					this.resolvedOwnerPlayerType = creationRefMap.get(this.getOwnerCreationRefId()).getClass();
				} else {
					throw new RuntimeException(MessageFormat.format("this.ownerCreationRefId != null. Not supported.\\n{0}", this));
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(MessageFormat.format("This should not happen\naction:\n{0}", this), e);
			}
		}
		return this.resolvedOwnerPlayerType;
	}

	public void setSimpleSettedValue(Object simpleSettedValue) {
		this.simpleSettedValue = simpleSettedValue;
	}

	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LinkedHashMap<String, Object> thisAsMap = new LinkedHashMap<>();
			thisAsMap.put("ownerSignatureStr", this.getOwnerSignatureStr());
			thisAsMap.put("ownerCreationId", this.getOwnerCreationId());
			thisAsMap.put("ownerCreationRefId", this.getOwnerCreationRefId());
			thisAsMap.put("settedSignatureStr", this.getSettedSignatureStr());
			thisAsMap.put("settedCreationId", this.getSettedCreationId());
			thisAsMap.put("settedCreationRefId", this.getSettedCreationRefId());
			thisAsMap.put("ownerPlayerType", this.getOwnerPlayerType());
			thisAsMap.put("actionType", this.getActionType());
			thisAsMap.put("fieldName", this.getFieldName());
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(thisAsMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
}
