package org.jsplayback.backend.hibernate;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.AssociationType;
import org.hibernate.type.BagType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.ListType;
import org.hibernate.type.SetType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsplayback.backend.IJsHbConfig;
import org.jsplayback.backend.IJsHbManager;
import org.jsplayback.backend.IJsHbReplayable;
import org.jsplayback.backend.IdentityRefKey;
import org.jsplayback.backend.SignatureBean;

public class JsHbManager implements IJsHbManager {
	private static Logger logger = LoggerFactory.getLogger(JsHbManager.class);
	
	private IJsHbConfig jsHbConfig = new JsHbConfig();
	ThreadLocal<Long> currIdTL = new ThreadLocal<Long>();
	ThreadLocal<Map<Long, Object>> objectByIdMapTL = new ThreadLocal<>();
	ThreadLocal<Map<IdentityRefKey, Long>> idByObjectMapTL = new ThreadLocal<>();
	ThreadLocal<IJsHbConfig> temporaryConfigurationTL = new ThreadLocal<IJsHbConfig>();
	ThreadLocal<Stack<JsHbBeanPropertyWriter>> jsHbBeanPropertyWriterStepStackTL = new ThreadLocal<Stack<JsHbBeanPropertyWriter>>();
	ThreadLocal<Stack<JsHbJsonSerializer>> JsHbJsonSerializerStepStackTL = new ThreadLocal<Stack<JsHbJsonSerializer>>(); 

	@Override
	public <T> JsHbResultEntity<T> createResultEntity(T result) {
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("createResultEntity for {0}", result != null? result.getClass(): "null"));
		}
		return new JsHbResultEntity<T>(result).configJsHbManager(this);
	}

	@Override
	public JsHbManager configure(IJsHbConfig jsHbConfig) {
		if (jsHbConfig == null) {
			throw new IllegalArgumentException("jsHbConfig can not be null");
		}
		if (logger.isDebugEnabled()) {
			logger.debug(MessageFormat.format("configure.  jsHbConfig:\n{0}", jsHbConfig));
		}
		this.jsHbConfig = jsHbConfig;
		return this;
	}

	private Map<Class, CompositeType> compositiesMap = new HashMap<>();

	private boolean initialed = false;

	@Override
	public IJsHbManager init() {
		if (logger.isDebugEnabled()) {
			logger.debug("init()");
		}
		this.compositiesMap.clear();
		this.collectComponentsMap();
		this.initialed = true;
		return this;
	}

	private void collectComponentsMap() {
		if (logger.isDebugEnabled()) {
			logger.debug("collectComponentsMap()");
		}
		for (String entityName : this.jsHbConfig.getSessionFactory().getAllClassMetadata().keySet()) {
			ClassMetadata classMetadata = this.jsHbConfig.getSessionFactory().getClassMetadata(entityName);
			
			List<Type> allPrpsAndId = new ArrayList<>();
			allPrpsAndId.addAll(Arrays.asList(classMetadata.getPropertyTypes()));
			allPrpsAndId.add(classMetadata.getIdentifierType());
			for (Type prpType : allPrpsAndId) {
				if (prpType instanceof CompositeType) {
					this.collectComponentsMapRecursive((CompositeType) prpType);
				}
			}
		}
	}

	private void collectComponentsMapRecursive(CompositeType compositeType) {
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("Collecting CompositeType:{0}", compositeType));
		}
		if (!this.compositiesMap.containsKey(compositeType.getReturnedClass())) {
			this.compositiesMap.put(compositeType.getReturnedClass(), compositeType);
			for (Type subPrpType : compositeType.getSubtypes()) {
				if (subPrpType instanceof CompositeType) {
					if (logger.isTraceEnabled()) {
						logger.trace(MessageFormat.format("Recursion on collect CompositeType: {0} -> {1}",
								compositeType, subPrpType));
					}
					this.collectComponentsMapRecursive((CompositeType) subPrpType);
				}
			}
		} else {
			CompositeType existingComponent = this.compositiesMap.get(compositeType.getReturnedClass());
			boolean isDifferent = false;
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format("Component already collected, verifying if the definition is the same: {0}", compositeType));
			}
			if (existingComponent.getSubtypes().length == compositeType.getSubtypes().length) {
				for (int i = 0; i < compositeType.getSubtypes().length; i++) {
					if (existingComponent.getSubtypes()[i].getReturnedClass() != compositeType.getSubtypes()[i]
							.getReturnedClass()) {
						isDifferent = true;
						break;
					}
				}
			} else {
				if (logger.isTraceEnabled()) {
					logger.trace(MessageFormat.format(
							"Component already collected, the both has the same definition, ok: {0}, {1}",
							existingComponent, compositeType));
				}
				isDifferent = true;
			}
			if (isDifferent) {
				throw new RuntimeException(MessageFormat.format("CompositeType's diferentes: {0}, {1}", existingComponent, compositeType));
			}
		}
	}

	@Override
	public String serializeSignature(SignatureBean signatureBean) {
		SignatureBeanJson signatureBeanJson = new SignatureBeanJson();

		ArrayList<String> rawValueList = new ArrayList();
		ArrayList<String> rawTypeList = new ArrayList();
		for (Object item : signatureBean.getRawKeyValues()) {
			if (item != null) {
				rawValueList.add(item.toString());
				rawTypeList.add(item.getClass().getName());
			} else {
				rawValueList.add(null);
				rawTypeList.add(null);
			}
		}

		signatureBeanJson.setClazzName(signatureBean.getClazz().getName());
		signatureBeanJson.setEntityName(signatureBean.getEntityName());
		signatureBeanJson.setPropertyName(signatureBean.getPropertyName());
		signatureBeanJson.setRawKeyValues(new String[rawValueList.size()]);
		signatureBeanJson.setRawKeyTypeNames(new String[rawValueList.size()]);
		signatureBeanJson.setRawKeyValues(rawValueList.toArray(signatureBeanJson.getRawKeyValues()));
		signatureBeanJson.setRawKeyTypeNames(rawTypeList.toArray(signatureBeanJson.getRawKeyTypeNames()));

		String result = "FOO BAA";
		if (this.jsHbConfig.getSignatureCrypto() != null) {
			result = this.jsHbConfig.getSignatureCrypto().encrypt(result);
		}

		ObjectMapper objectMapper = new ObjectMapper();
		StringWriter writer = new StringWriter();
		try {
			objectMapper.writeValue(writer, signatureBeanJson);
		} catch (JsonGenerationException e) {
			throw new RuntimeException("Isso nao deveria acontencer!", e);
		} catch (JsonMappingException e) {
			throw new RuntimeException("Isso nao deveria acontencer!", e);
		} catch (IOException e) {
			throw new RuntimeException("Isso nao deveria acontencer!", e);
		}
		String resultStr = writer.toString();
		if (this.jsHbConfig.getSignatureCrypto() != null) {
			resultStr = this.jsHbConfig.getSignatureCrypto().encrypt(resultStr);
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"serializeSignature(). encrypting. original json signature: ''{0}'', encripted signature: ''{1}''",
						writer.toString(), resultStr));
			}
		} else {
			resultStr = Base64.encodeBase64URLSafeString(resultStr.getBytes());
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"serializeSignature(). Using simple ''base64 url safe'' from json signature. original json signature: ''{0}'', base64 signature: ''{1}''",
						writer.toString(), resultStr));
			}
		}
		return resultStr;
	}
	
	@Override
	public SignatureBean deserializeSignature(String signatureStr) {
		String decryptedSignatureStr = signatureStr;

		if (this.jsHbConfig.getSignatureCrypto() != null) {
			decryptedSignatureStr = this.jsHbConfig.getSignatureCrypto().decrypt(signatureStr);
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"serializeSignature(). decrypting. original json signature: ''{0}'', dncripted signature: ''{1}''",
						decryptedSignatureStr, signatureStr));
			}
		} else {
			decryptedSignatureStr = new String(Base64.decodeBase64(signatureStr));
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"serializeSignature(). Using simple ''base64 url safe'' from json signature. original json signature: ''{0}'', base64 signature: ''{1}''",
						decryptedSignatureStr, signatureStr));
			}
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		StringReader reader = new StringReader(decryptedSignatureStr);
		SignatureBeanJson signatureBeanJson;
		try {
			signatureBeanJson = objectMapper.readValue(reader, SignatureBeanJson.class);
		} catch (IOException e) {
			throw new RuntimeException("This should not happen", e);
		}
		SignatureBean signatureBean = new SignatureBean();
		
		ArrayList<Object> rawObjValueList = new ArrayList();
		for (int i = 0; i < signatureBeanJson.getRawKeyTypeNames().length; i++) {
			Class<?> itemType;
			try {
				itemType = Class.forName(signatureBeanJson.getRawKeyTypeNames()[i]);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("This should not happen", e);
			}
			String itemValueStr = signatureBeanJson.getRawKeyValues()[i]; 
			Method valueOfMethod;
			try {
				if (itemType == String.class) {
					valueOfMethod = null;
				} else {
					valueOfMethod = itemType.getMethod("valueOf", String.class);					
				}
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("This should not happen", e);
			} catch (SecurityException e) {
				throw new RuntimeException("This should not happen", e);
			}
			Object itemValue;
			try {
				if (valueOfMethod == null) {
					itemValue = itemValueStr;
				} else {
					itemValue = valueOfMethod.invoke(null, itemValueStr);					
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException("This should not happen", e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("This should not happen", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("This should not happen", e);
			}
			rawObjValueList.add(itemValue);
		}
		signatureBean.setRawKeyValues(rawObjValueList.toArray());
		try {
			signatureBean.setClazz(Class.forName(signatureBeanJson.getClazzName()));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("This should not happen", e);
		}
		signatureBean.setEntityName(signatureBeanJson.getEntityName());
		signatureBean.setPropertyName(signatureBeanJson.getPropertyName());
		signatureBean.setSignature(signatureStr);
		
		return signatureBean;
	}

	private static Pattern rxCollectionRole = Pattern.compile("^(.*\\.)([^.]+)$");

	@Override
	public SignatureBean generateLazySignature(PersistentCollection persistentCollection) {
		// SessionImplementor ssImplementor = null;
		// Class ownerClass = persistentCollection.getOwner().getClass();
		//
		// if (HibernateProxy.class.isAssignableFrom(ownerClass)) {
		// ownerClass = ownerClass.getSuperclass();
		// }

		Matcher matcher = rxCollectionRole.matcher(persistentCollection.getRole());
		if (!matcher.find()) {
			throw new RuntimeException(MessageFormat.format(
					"Collection role nao se encaixa no padrao esperado: ''{0}''", persistentCollection.getRole()));
		}

		Class<?> ownerClass = persistentCollection.getOwner().getClass();
		String fieldName = matcher.group(2);
		Object ownerValue = persistentCollection.getOwner();
		Object fieldValue = persistentCollection;

		SignatureBean signatureBean = this.generateLazySignatureForRelashionship(ownerClass, fieldName, ownerValue, fieldValue);
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format(
					"generateLazySignature(). signatureBean:\nsignatureBean:\n{0}",
					signatureBean));
		}
		
		return signatureBean;
	}

	@Override
	public SignatureBean generateLazySignatureForRelashionship(Class<?> ownerClass, String fieldName, Object ownerValue,
			Object fieldValue) {
		if (!this.isRelationship(ownerClass, fieldName)) {
			throw new RuntimeException("Nao eh um relacionamento: " + ownerClass + "->" + fieldName);
		}
		ClassMetadata classMetadata = this.jsHbConfig.getSessionFactory().getClassMetadata(ownerClass);
		Type prpType = classMetadata.getPropertyType(fieldName);
		if (!(prpType instanceof AssociationType)) {
			throw new RuntimeException("Tipo inesperado para " + ownerClass + "->" + fieldName + ": " + prpType);
		}

		SignatureBean signatureBean = new SignatureBean();
		AssociationType assType = (AssociationType) classMetadata.getPropertyType(fieldName);
		Object idValue = null;
		if (assType instanceof CollectionType) {
			if (ownerValue == null) {
				throw new IllegalArgumentException(
						"ownerValue can not be null em caso de CollectionType: " + ownerClass + "->" + fieldName);
			}
			CollectionType collType = (CollectionType) assType;
			idValue = collType.getKeyOfOwner(ownerValue,
					(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession());
			signatureBean.setClazz(ownerClass);
			signatureBean.setPropertyName(fieldName);
		} else {
			classMetadata = this.jsHbConfig.getSessionFactory().getClassMetadata(assType.getReturnedClass());
			idValue = classMetadata.getIdentifier(fieldValue,
					(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession());
			signatureBean.setClazz(assType.getReturnedClass());
		}
		signatureBean.setEntityName(classMetadata.getEntityName());

		JsHbStatment jsHbStatment = new JsHbStatment();

		Type hbIdType = classMetadata.getIdentifierType();
		try {
			hbIdType.nullSafeSet(jsHbStatment, idValue, 0,
					(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession());
		} catch (HibernateException e) {
			throw new RuntimeException("This should not happen", e);
		} catch (SQLException e) {
			throw new RuntimeException("This should not happen", e);
		}
		signatureBean.setRawKeyValues(jsHbStatment.getInternalValues());
		
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format(
					"generateLazySignatureForRelashionship().signatureBean:\n{0}",
					signatureBean));
		}
		
		return signatureBean;
	}

	@Override
	public SignatureBean generateLazySignature(HibernateProxy hibernateProxy) {
		Class entityClass = hibernateProxy.getClass().getSuperclass();
		SignatureBean signatureBean = new SignatureBean();
		signatureBean.setClazz(entityClass);
		signatureBean.setEntityName(entityClass.getName());
		signatureBean.setPropertyName(null);
		JsHbStatment jsHbStatment = new JsHbStatment();

		ClassMetadata classMetadata = this.jsHbConfig.getSessionFactory().getClassMetadata(entityClass);

		@SuppressWarnings("deprecation")
		Object idValue = classMetadata.getIdentifier(hibernateProxy,
				(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession());
		Type hbIdType = classMetadata.getIdentifierType();
		try {
			hbIdType.nullSafeSet(jsHbStatment, idValue, 0,
					(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession());
		} catch (HibernateException e) {
			throw new RuntimeException("This should not happen", e);
		} catch (SQLException e) {
			throw new RuntimeException("This should not happen", e);
		}
		signatureBean.setRawKeyValues(jsHbStatment.getInternalValues());
		
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format(
					"generateLazySignature(). signatureBean:\nsignatureBean:\n{0}",
					signatureBean));
		}
		
		return signatureBean;
	}

	@Override
	public SignatureBean generateSignature(Object nonHibernateProxy) {
		if (nonHibernateProxy instanceof HibernateProxy) {
			throw new RuntimeException("nonHibernateProxy instanceof HibernateProxy: " + nonHibernateProxy);
		}
		Class entityClass = nonHibernateProxy.getClass();
		SignatureBean signatureBean = new SignatureBean();
		signatureBean.setClazz(entityClass);
		signatureBean.setEntityName(entityClass.getName());
		signatureBean.setPropertyName(null);
		JsHbStatment jsHbStatment = new JsHbStatment();

		ClassMetadata classMetadata = this.jsHbConfig.getSessionFactory().getClassMetadata(entityClass);

		@SuppressWarnings("deprecation")
		Object idValue = classMetadata.getIdentifier(nonHibernateProxy,
				(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession());
		Type hbIdType = classMetadata.getIdentifierType();
		try {
			hbIdType.nullSafeSet(jsHbStatment, idValue, 0,
					(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession());
		} catch (HibernateException e) {
			throw new RuntimeException("This should not happen", e);
		} catch (SQLException e) {
			throw new RuntimeException("This should not happen", e);
		}
		signatureBean.setRawKeyValues(jsHbStatment.getInternalValues());
		
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format(
					"generateSignature(Object nonHibernateProxy). signatureBean:\n{0}",
					signatureBean));
		}
		
		return signatureBean;
	}

	@Override
	public <T> T getBySignature(SignatureBean signature) {
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("getBySignature(). Begin. \nsignatureBean:\n{0}",
					signature));
		}
		
		ClassMetadata classMetadata = this.jsHbConfig.getSessionFactory().getClassMetadata(signature.getClazz());

		Type hbIdType = classMetadata.getIdentifierType();

		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("getBySignature(). Hibernate id Type: ''{0}''",
					hbIdType));
		}
		
//		Serializable idValue = (Serializable) hbIdType.resolve(signature.getRawKeyValues(),
//				(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession(), null);
		Serializable idValue = null;
		
		JsHbResultSet jsHbResultSet = new JsHbResultSet(signature.getRawKeyValues());
		try {
			idValue = (Serializable) hbIdType.nullSafeGet(jsHbResultSet, jsHbResultSet.getColumnNames(),
					(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession(), null);
		} catch (HibernateException e) {
			throw new RuntimeException("This should not happen. prpType: ");
		} catch (SQLException e) {
			throw new RuntimeException("This should not happen. prpType: ");
		}
		
		if (idValue.getClass().isArray()) {
			if (((Object[])idValue).length == 1) {
				idValue = (Serializable) ((Object[])idValue)[0];
			}
		}
		Object owner = this.jsHbConfig.getSessionFactory().getCurrentSession().get(signature.getClazz(), idValue);

		Object result = owner;

		Type propertyType = null;
		if (signature.getPropertyName() != null) {
			// significa que eh uma collection, mas no futuro podera ser tambem uma propriedade lazy, como um blob por exemplo!
			Type prpType = classMetadata.getPropertyType(signature.getPropertyName());
			if (logger.isWarnEnabled()) {
				logger.warn(MessageFormat
						.format("getBySignature(). propery Type: ''{0}''. We are inferring this is an 'one to many'"
								+ " relationship because propertyName is not null, on the"
								+ " future this will not be always true, there will exists"
								+ " non collection lazy properties like Blob's for instance.", prpType));
			}
			Collection resultColl = null;
			if (prpType instanceof CollectionType) {
				if (prpType instanceof SetType) {
					resultColl = new LinkedHashSet<>();
				} else if (prpType instanceof ListType) {
					throw new RuntimeException("Not supported. prpType: " + prpType);
				} else if (prpType instanceof BagType) {
					throw new RuntimeException("Not supported. prpType: " + prpType);
				} else {
					throw new RuntimeException("This should not happen. prpType: " + prpType);
				}
			} else {
				throw new RuntimeException("This should not happen. prpType: " + prpType);
			}
			Collection persistentCollection = (Collection) classMetadata.getPropertyValue(owner,
					signature.getPropertyName(),
					this.jsHbConfig.getSessionFactory().getCurrentSession().getEntityMode());
			for (Object item : persistentCollection) {
				resultColl.add(item);
			}
			result = resultColl;
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("getBySignature(). \nsignatureBean:\n{0}\nresult.getClass(): ''{1}''",
					signature, (result != null ? result.getClass().toString() : "null")));
		}

		return (T) result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isNeverSigned(Class clazz) {
		return this.jsHbConfig.getNeverSignedClasses().contains(clazz);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isPersistentClassOrComponent(Class clazz) {
		return this.compositiesMap.containsKey(clazz)
				|| this.jsHbConfig.getSessionFactory().getClassMetadata(clazz) != null;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean isPersistentClass(Class clazz) {
		return this.jsHbConfig.getSessionFactory().getClassMetadata(clazz) != null;
	}

	@Override
	public Object getHibernateObjectId(Object object) {
		if (logger.isTraceEnabled()) {
			logger.trace("getHibernateObjectId()");
		}
		Class entityClass = object.getClass();
		if (object instanceof HibernateProxy) {
			entityClass = object.getClass().getSuperclass();
		}

		@SuppressWarnings("deprecation")
		Object idValue = this.jsHbConfig.getSessionFactory().getClassMetadata(entityClass).getIdentifier(object,
				(SessionImplementor) this.jsHbConfig.getSessionFactory().getCurrentSession());
		return idValue;
	}
	
	@Override
	public String getHibernateIdName(Class clazz) {

		return this.jsHbConfig.getSessionFactory().getClassMetadata(clazz).getIdentifierPropertyName();
	}

	@Override
	public IJsHbConfig getJsHbConfig() {
		if (this.temporaryConfigurationTL.get() != null) {
			return this.temporaryConfigurationTL.get();
		} else {
			return this.jsHbConfig;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void startSuperSync() {
		if (logger.isTraceEnabled()) {
			logger.trace("startSuperSync()");
		}
		if (!this.initialed) { // pode parecer desnecessario mas evita o overead
								// de obtencao de lock no synchronized logo
								// abaixo.
			synchronized (this) {
				if (!this.initialed) {
					this.init();
				}
			}
		}
		this.currIdTL.set(0L);
		this.objectByIdMapTL.set(new HashMap<Long, Object>());
		this.idByObjectMapTL.set(new HashMap<IdentityRefKey, Long>());
		this.jsHbBeanPropertyWriterStepStackTL.set(new Stack<JsHbBeanPropertyWriter>());
		this.JsHbJsonSerializerStepStackTL.set(new Stack<JsHbJsonSerializer>());
	}

	@Override
	public void stopSuperSync() {
		if (logger.isTraceEnabled()) {
			logger.trace("stopSuperSync()");
		}
		this.currIdTL.set(null);
		this.objectByIdMapTL.set(null);
		this.idByObjectMapTL.set(null);
		this.jsHbBeanPropertyWriterStepStackTL.set(null);
		this.JsHbJsonSerializerStepStackTL.set(null);
		
		this.temporaryConfigurationTL.set(null);
	}

	private void validateStarted() {
		if (this.currIdTL.get() == null) {
			throw new RuntimeException("Not started");
		}
	}

	@Override
	public Map<Long, Object> getObjectByIdMap() {
		this.validateStarted();
		return this.objectByIdMapTL.get();
	}

	@Override
	public Map<IdentityRefKey, Long> getIdByObjectMap() {
		this.validateStarted();
		return this.idByObjectMapTL.get();
	}
	
	@Override
	public Stack<JsHbBeanPropertyWriter> getJsHbBeanPropertyWriterStepStack() {
		return this.jsHbBeanPropertyWriterStepStackTL.get();
	}
	
	@Override
	public Stack<JsHbJsonSerializer> getJsHbJsonSerializerStepStackTL() {
		return this.JsHbJsonSerializerStepStackTL.get();
	}

	@Override
	public Long getCurrId() {
		this.validateStarted();
		return this.currIdTL.get();
	}

	@Override
	public void currIdPlusPlus() {
		this.validateStarted();
		this.currIdTL.set(this.currIdTL.get() + 1);
	}

	@Override
	public boolean isStarted() {
		return this.currIdTL.get() != null;
	}

	@Override
	public boolean isRelationship(Class<?> clazz, String fieldName) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz can not be null");
		}
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new IllegalArgumentException("fieldName can not be null");
		}
		
		ClassMetadata classMetadata = this.jsHbConfig.getSessionFactory().getClassMetadata(clazz);
		CompositeType compositeType = null;
		if (classMetadata == null) {
			compositeType = this.compositiesMap.get(clazz);
			if (compositeType == null) {
				throw new RuntimeException("Class is not mapped and is not a know CompositeType: " + clazz);
			}
		}
		Type prpType = null;
		if (classMetadata != null) {
			for (int i = 0; i < classMetadata.getPropertyNames().length; i++) {
				String prpNameItem = classMetadata.getPropertyNames()[i];
				if (prpNameItem.equals(fieldName)) {
					prpType = classMetadata.getPropertyTypes()[i];
					break;
				}
			}			
		} else {
			for (int i = 0; i < compositeType.getPropertyNames().length; i++) {
				String prpNameItem = compositeType.getPropertyNames()[i];
				if (prpNameItem.equals(fieldName)) {
					prpType = compositeType.getSubtypes()[i];
					break;
				}
			}
		}
		
		boolean resultBool = false;
		if (prpType == null) {
			resultBool = false;
		} else {
			if (prpType instanceof AssociationType) {
				resultBool = true;
			} else {
				resultBool = false;
			}
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(
					MessageFormat.format("isRelationship(). clazz: ''{0}''; fieldName: ''{1}''. return: ", clazz, fieldName, resultBool));
		}
		
		return resultBool;
	}

	@Override
	public boolean isComponent(Class<?> clazz, String fieldName) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz can not be null");
		}
		if (fieldName == null || fieldName.trim().isEmpty()) {
			throw new IllegalArgumentException("fieldName can not be null");
		}
		ClassMetadata classMetadata = this.jsHbConfig.getSessionFactory().getClassMetadata(clazz);
		CompositeType compositeType = null;
		if (classMetadata == null) {
			compositeType = this.compositiesMap.get(clazz);
			if (compositeType == null) {
				throw new RuntimeException("Class is not mapped and is not a know CompositeType: " + clazz);
			}
		}
		
		Type prpType = null;
		if (classMetadata != null) {
			for (int i = 0; i < classMetadata.getPropertyNames().length; i++) {
				String prpNameItem = classMetadata.getPropertyNames()[i];
				if (prpNameItem.equals(fieldName)) {
					prpType = classMetadata.getPropertyTypes()[i];
					break;
				}
			}			
		} else {
			for (int i = 0; i < compositeType.getPropertyNames().length; i++) {
				String prpNameItem = compositeType.getPropertyNames()[i];
				if (prpNameItem.equals(fieldName)) {
					prpType = compositeType.getSubtypes()[i];
					break;
				}
			}
		}
		
		boolean resultBool = false;
		if (prpType == null) {
			resultBool =  false;
		} else {
			if (prpType instanceof ComponentType) {
				resultBool =  true;
			} else {
				resultBool =  false;
			}
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(
					MessageFormat.format("isComponent(). clazz: ''{0}''; fieldName: ''{1}''. return: {2}", clazz, fieldName, resultBool));
		}
		
		return resultBool;
	}

	@Override
	public IJsHbManager overwriteConfigurationTemporarily(IJsHbConfig newConfig) {
		if (logger.isTraceEnabled()) {
			logger.trace(
					MessageFormat.format("overwriteConfigurationTemporarily(). newConfig:\n {0}'", newConfig));
		}
		this.temporaryConfigurationTL.set(newConfig);
		return this;
	}
	
	@Override
	public IJsHbReplayable prepareReplayable(JsHbPlayback playback) {
//		throw new RuntimeException("");
//		this.temporaryConfigurationTL.set(newConfig);
//		return null;
		if (logger.isTraceEnabled()) {
			logger.trace(
					MessageFormat.format("prepareReplayable(). playback:\n {0}'", playback));
		}
		return new JsHbReplayable().configJsHbManager(this).loadPlayback(playback);
	}
}
/*gerando conflito*/