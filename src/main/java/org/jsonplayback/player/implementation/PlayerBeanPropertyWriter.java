package org.jsonplayback.player.implementation;

import java.util.Stack;

import org.hibernate.proxy.HibernateProxy;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.PlayerMetadatas;
import org.jsonplayback.player.LazyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;

public class PlayerBeanPropertyWriter extends BeanPropertyWriter {
	private static Logger logger = LoggerFactory.getLogger(PlayerBeanPropertyWriter.class);

	private IPlayerManagerImplementor playerManager;

//	private Class<?> componentOwnerClass = null;
	private Class<?> relationshipOwnerClass = null;
	private ThreadLocal<Stack<Object>> currOwnerStackTL = new ThreadLocal<>();
	private BeanPropertyDefinition beanPropertyDefinition = null;
	private boolean isPersistent;
	private boolean isPlayerObjectId;
	private boolean isMetadatasPlayerObjectId;
	private LazyProperty lazyProperty;
	private PlayerJsonSerializer playerJsonSerializerForLazyProperty;

	public PlayerBeanPropertyWriter configPlayerBeanPropertyWriterForLazyProperty(PlayerJsonSerializer playerJsonSerializerForLazyProperty) {
		this.playerJsonSerializerForLazyProperty = playerJsonSerializerForLazyProperty;
		return this;
	}

	public LazyProperty getLazyProperty() {
		return lazyProperty;
	}
	
	protected ThreadLocal<Stack<Object>> getCurrOwnerStackTL() {
		if (currOwnerStackTL.get() == null) {
			currOwnerStackTL.set(new Stack<>());
		}
		return currOwnerStackTL;			
	}

	public PlayerBeanPropertyWriter loadLazyProperty(LazyProperty lazyProperty) {
		this.lazyProperty = lazyProperty;
		return this;
	}
	
	public boolean isMetadatasPlayerObjectId() {
		return isMetadatasPlayerObjectId;
	}

	public PlayerBeanPropertyWriter loadIsMetadatasPlayerObjectId(boolean isPlayerObjectIdOnMetadatas) {
		this.isMetadatasPlayerObjectId = isPlayerObjectIdOnMetadatas;
		return this;
	}

	public boolean getIsPlayerObjectId() {
		return isPlayerObjectId;
	}

	public PlayerBeanPropertyWriter loadIsPlayerObjectId(boolean isPlayerObjectId) {
		this.isPlayerObjectId = isPlayerObjectId;
		return this;
	}

	public boolean getIsPersistent() {
		return isPersistent;
	}

	public PlayerBeanPropertyWriter loadIsPersistent(boolean isPersistent) {
		this.isPersistent = isPersistent;
		return this;
	}

	public BeanPropertyDefinition getBeanPropertyDefinition() {
		return beanPropertyDefinition;
	}

	public PlayerBeanPropertyWriter loadBeanPropertyDefinition(BeanPropertyDefinition beanPropertyDefinition) {
		this.beanPropertyDefinition = beanPropertyDefinition;
		return this;
	}

	public Object getCurrOwner() {
		if (this.getCurrOwnerStackTL().get().size() > 0) {
			return getCurrOwnerStackTL().get().peek();
		} else {
			return null;
	}
	}

//	public Class<?> getComponentOwnerClass() {
//		return componentOwnerClass;
//	}

	public Class<?> getRelationshipOwnerClass() {
		return relationshipOwnerClass;
	}

	public PlayerBeanPropertyWriter loadRelationshipOwnerClass(Class<?> relationshipOwnerClass) {
		this.relationshipOwnerClass = relationshipOwnerClass;
		return this;
	}

	public PlayerBeanPropertyWriter configManager(IPlayerManagerImplementor manager) {
		this.playerManager = manager;
		return this;
	}

	public PlayerBeanPropertyWriter(BeanPropertyWriter propertyWriter) {
		super(propertyWriter);
	}

	public void initializeSerializer(JsonGenerator gen, SerializerProvider prov) throws Exception {
		// then find serializer to use
		JsonSerializer<Object> ser = _serializer;
		if (ser == null) {
			Class<?> cls = this.getType().getRawClass();
			PropertySerializerMap m = _dynamicSerializers;
			ser = m.serializerFor(this.getPropertyType());
			if (ser == null) {
				ser = _findAndAddDynamic(m, cls, prov);
			}
		}
	}

	@Override
	public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {

		try {
			if (this.playerManager.isStarted()) {
				if (!this.isMetadatasPlayerObjectId) {
				this.playerManager.getPlayerBeanPropertyWriterStepStack().push(this);
				} else {
					PlayerMetadatas metadatas = (PlayerMetadatas) bean;
					this.playerManager.getPlayerBeanPropertyWriterStepStack().push(metadatas.getOriginalPlayerObjectIdPropertyWriter());
			}
			}
			
			//?!?!?!?! 
			if (!this.isMetadatasPlayerObjectId) {					
				this.getCurrOwnerStackTL().get().push(bean);
			} else {
				PlayerMetadatas metadatas = (PlayerMetadatas) bean;
				this.getCurrOwnerStackTL().get().push(metadatas.getOriginalPlayerObjectIdOwner());
			}
			
			if (this.lazyProperty != null) {
				JsonSerializer<Object> delegateSerializer = this.getSerializer();
				if (delegateSerializer == null) {
					delegateSerializer = prov.findValueSerializer(this.getPropertyType());					
				}
				if (!(this.getSerializer() instanceof PlayerJsonSerializer)) {
					super._serializer = new PlayerJsonSerializer(delegateSerializer).configManager(this.playerManager);
				}
			}

			if (this.playerManager.isStarted()) {
				if (!this.isMetadatasPlayerObjectId) {
					super.serializeAsField(bean, gen, prov);					
				} else {
					PlayerMetadatas metadatas = (PlayerMetadatas) bean;
					if (metadatas.getPlayerObjectId() != null) {
						gen.writeFieldName(_name);
						metadatas.getOriginalPlayerObjectIdPropertyWriter().serializeAsElement(
								metadatas.getOriginalPlayerObjectIdOwner(), 
								gen, 
								prov);						
					}
				}
			} else {
			super.serializeAsField(bean, gen, prov);
			}	
		} finally {
			this.getCurrOwnerStackTL().get().pop();
			if (this.playerManager.isStarted()) {
				PlayerBeanPropertyWriter propertyWriterPop = this.playerManager.getPlayerBeanPropertyWriterStepStack().pop();
				if (!this.isMetadatasPlayerObjectId) {
					if (propertyWriterPop != this) {
						throw new RuntimeException("This should not happen");
					}					
				} else {
					PlayerMetadatas metadatas = (PlayerMetadatas) bean;
					if (propertyWriterPop != metadatas.getOriginalPlayerObjectIdPropertyWriter()) {
						throw new RuntimeException("This should not happen");
					}
				}
			}
		}
	}

	@Override
	public Class<?> getPropertyType() {
		Class originalClass = super.getPropertyType();
		if (originalClass != null && HibernateProxy.class.isAssignableFrom(originalClass)) {
			return originalClass.getSuperclass();
		} else {
			return super.getPropertyType();
		}
	}

	@Override
	public JavaType getType() {
		JavaType originalClass = super.getType();
		if (originalClass != null && HibernateProxy.class.isAssignableFrom(originalClass.getRawClass())) {
			return originalClass.getSuperClass();
		} else {
			return originalClass;
		}
	}

	public void findFieldPlayerObjectIdentifierValue(Object bean, SerializerProvider prov, PlayerMetadatas backendMetadatas)
			throws Exception {
		try {
			if (this.playerManager.isStarted()) {
				this.playerManager.getPlayerBeanPropertyWriterStepStack().push(this);
			}
			this.getCurrOwnerStackTL().get().push(bean);

			// inlined 'get()'
			final Object value = (_accessorMethod == null) ? _field.get(bean) : _accessorMethod.invoke(bean);

			backendMetadatas.setPlayerObjectId(value);
			backendMetadatas.setOriginalPlayerObjectIdPropertyWriter(this);
			backendMetadatas.setOriginalPlayerObjectIdOwner(bean);
		} finally {
			this.getCurrOwnerStackTL().get().pop();
			if (this.playerManager.isStarted()) {
				PlayerBeanPropertyWriter propertyWriterPop = this.playerManager.getPlayerBeanPropertyWriterStepStack().pop();
				if (propertyWriterPop != this) {
					throw new RuntimeException("This should not happen");
				}
			}
		}
	}
	
	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder(40);
        sb
        	.append("PlayerBeanPropertyWriter with")
//        	.append("componentOwnerClass='").append(componentOwnerClass).append("', ")
        	.append("relationshipOwnerClass='").append(relationshipOwnerClass).append("', ")
        	.append("isPersistent=").append(isPersistent).append(", ")
        	.append("isPlayerObjectId=").append(isPlayerObjectId).append(", ")
        	.append("relationshipOwnerClass='").append(relationshipOwnerClass).append("'")
        	.append(" from original BeanPropertyWriter={ ").append(super.toString()).append(" }");
		return sb.toString();
	}
}
/*gerando conflito*/