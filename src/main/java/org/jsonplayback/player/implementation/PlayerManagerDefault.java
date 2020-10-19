package org.jsonplayback.player.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.Base64;
import org.jsonplayback.player.IDirectRawWriter;
import org.jsonplayback.player.IDirectRawWriterWrapper;
import org.jsonplayback.player.IPlayerConfig;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.IdentityRefKey;
import org.jsonplayback.player.LazyProperty;
import org.jsonplayback.player.ObjPersistenceMode;
import org.jsonplayback.player.ObjPersistenceSupport;
import org.jsonplayback.player.PlayerMetadatas;
import org.jsonplayback.player.PlayerSnapshot;
import org.jsonplayback.player.RecorderTypeAlias;
import org.jsonplayback.player.SignatureBean;
import org.jsonplayback.player.util.PathEntry;
import org.jsonplayback.player.util.ReflectionNamesDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.PropertyWriter;

public class PlayerManagerDefault implements IPlayerManagerImplementor {
	private static final String OBJ_PERSISTENCE_MODE = "jsonplayback.objPersistenceMode"; 
	private static Logger logger = LoggerFactory.getLogger(PlayerManagerDefault.class);
	private static Properties envPrps = new Properties(); 
	
	static {
		InputStream is = PlayerManagerDefault.class.getClassLoader().getResourceAsStream("environment.properties");
		try {
			envPrps.load(is);
		} catch (IOException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
	
	private IPlayerConfig config = new PlayerConfig();
	ThreadLocal<Long> currIdTL = new ThreadLocal<Long>();
	ThreadLocal<Map<Long, Object>> objectByIdMapTL = new ThreadLocal<>();
	ThreadLocal<Map<IdentityRefKey, Long>> idByObjectMapTL = new ThreadLocal<>();
	ThreadLocal<Map<IdentityRefKey, PlayerMetadatas>> metadatasCacheMapTL = new ThreadLocal<>();
	ThreadLocal<IPlayerConfig> temporaryConfigurationTL = new ThreadLocal<IPlayerConfig>();
	ThreadLocal<Map<IdentityRefKey, OwnerAndProperty>> registeredComponentOwnersTL = new ThreadLocal<>();
	ThreadLocal<Stack<PlayerBeanPropertyWriter>> playerBeanPropertyWriterStepStackTL = new ThreadLocal<Stack<PlayerBeanPropertyWriter>>();
	ThreadLocal<Stack<PlayerJsonSerializer>> playerJsonSerializerStepStackTL = new ThreadLocal<Stack<PlayerJsonSerializer>>(); 
	ThreadLocal<Stack<PlayerMetadatas>> playerMetadatasWritingStackTL = new ThreadLocal<Stack<PlayerMetadatas>>();

	@Override
	public <T> PlayerSnapshot<T> createPlayerSnapshot(T result) {
		if (logger.isTraceEnabled()) {
			logger.trace(
					MessageFormat.format("createResultEntity for {0}", result != null ? result.getClass() : "null"));
		}
		return new PlayerSnapshot<T>(result).configManager(this);
	}

	@Override
	public PlayerManagerDefault configure(IPlayerConfig config) {
		if (config == null) {
			throw new IllegalArgumentException("config can not be null");
		}
		if (logger.isDebugEnabled()) {
			logger.debug(MessageFormat.format("configure.  config:\n{0}", config));
		}
		this.config = config;
		this.objPersistenceMode = getObjPersistenceModeStatic();
		
		if (this.objPersistenceMode.equals(ObjPersistenceMode.HB3)) {
			this.objPersistenceSupport = config.getObjPersistenceSupport();
		} else if (this.objPersistenceMode.equals(ObjPersistenceMode.HB4)) {
			this.objPersistenceSupport = config.getObjPersistenceSupport();
		} else if (this.objPersistenceMode.equals(ObjPersistenceMode.HB5)) {
			this.objPersistenceSupport = config.getObjPersistenceSupport();
		} else if (this.objPersistenceMode.equals(ObjPersistenceMode.JPA)) {
			this.objPersistenceSupport = config.getObjPersistenceSupport();
		} else if (this.objPersistenceMode.equals(ObjPersistenceMode.CUSTOMIZED_PERSISTENCE)) {
			this.objPersistenceSupport = config.getObjPersistenceSupport();
		} else {
			throw new RuntimeException(
				"Object Persistent mode not supported: " +
				this.objPersistenceMode +
				". Colaborate, help us to support this hibernate version.");
		}
		return this;
	}

	private boolean initialed = false;

	@Override
	public IPlayerManager init() {
		synchronized (this) {
			if (!this.initialed) {
				if (logger.isDebugEnabled()) {
					logger.debug("init()");
				}
				this.getObjPersistenceSupport().init(this);
				this.loadManagedTypes();
				if (this.config.getManagerId().contains("" + this.config.getManagerIdSignaturePrefixFlag())) {
					throw new RuntimeException("Manager id ("
							+ this.config.getManagerId()
							+ ") can not contains ManagerIdSignaturePrefixFlag ("
							+ this.config.getManagerIdSignaturePrefixFlag()+")");
				}
				this.initialed = true;
			}
			return this;
		}		
	}
	
	private void loadManagedTypes() {
		this.managedTypeMap = new HashMap<>();
		LinkedHashSet<Class> allManaged = new LinkedHashSet<>();
		allManaged.addAll(this.getObjPersistenceSupport().allManagedTypes());
		allManaged.addAll(this.config.addictionalManagedTypes());
		for (Class<?> type : allManaged) {
			RecorderTypeAlias recorderTypeAlias = type.getAnnotation(RecorderTypeAlias.class);
			if(recorderTypeAlias != null) {
				this.managedTypeMap.put(recorderTypeAlias.value(), type);
			}
			this.managedTypeMap.put(type.getName(), type);
		}
	}

	@Override
	public String serializeSignature(SignatureBean signatureBean) {
		SignatureBeanJson signatureBeanJson = new SignatureBeanJson();

		ArrayList<String> rawValueList = new ArrayList<String>();
		ArrayList<String> rawTypeList = new ArrayList<String>();
//		for (Object item : signatureBean.getRawKeyValues()) {
//			if (item != null) {
//				rawValueList.add(item.toString());
//				rawTypeList.add(item.getClass().getName());
//			} else {
//				rawValueList.add(null);
//				rawTypeList.add(null);
//			}
//		}
		

		signatureBeanJson.setClazzName(signatureBean.getClazz().getName());
		signatureBeanJson.setPropertyName(signatureBean.getPropertyName());
//		signatureBeanJson.setRawKeyValues(new String[rawValueList.size()]);
//		signatureBeanJson.setRawKeyTypeNames(new String[rawValueList.size()]);
//		signatureBeanJson.setRawKeyValues(rawValueList.toArray(signatureBeanJson.getRawKeyValues()));
//		signatureBeanJson.setRawKeyTypeNames(rawTypeList.toArray(signatureBeanJson.getRawKeyTypeNames()));
		signatureBeanJson.setStringifiedObjectId(signatureBean.getStringifiedObjectId());
		signatureBeanJson.setIsColl(signatureBean.getIsColl());
		signatureBeanJson.setIsComp(signatureBean.getIsComp());

		String result = "FOO BAA";
		if (this.config.getSignatureCrypto() != null) {
			result = this.config.getSignatureCrypto().encrypt(result);
		}

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_DEFAULT);
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
		if (this.config.getSignatureCrypto() != null) {
			resultStr = this.config.getSignatureCrypto().encrypt(resultStr);
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
		return this.config.getManagerId() + this.config.getManagerIdSignaturePrefixFlag() + resultStr;
	}
	
	@Override
	public SignatureBean deserializeSignature(String signatureStr) {
		if (!this.isManagerOfSignature(signatureStr)) {
			throw new RuntimeException(
					"I (manager id: " + this.config.getManagerId() + ")"
							+ " am not manager for signature: " + signatureStr);
		}
		
		int indexOfFlag = signatureStr.indexOf(this.config.getManagerIdSignaturePrefixFlag());
		String decryptedSignatureStr = signatureStr.substring(indexOfFlag);

		if (this.config.getSignatureCrypto() != null) {
			decryptedSignatureStr = this.config.getSignatureCrypto().decrypt(decryptedSignatureStr);
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"serializeSignature(). decrypting. original json signature: ''{0}'', dncripted signature: ''{1}''",
						decryptedSignatureStr, signatureStr));
			}
		} else {
			decryptedSignatureStr = new String(Base64.decodeBase64(decryptedSignatureStr));
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
		
//		ArrayList<Object> rawObjValueList = new ArrayList();
//		for (int i = 0; i < signatureBeanJson.getRawKeyTypeNames().length; i++) {
//			Class<?> itemType;
//			try {
//				itemType = Class.forName(signatureBeanJson.getRawKeyTypeNames()[i]);
//			} catch (ClassNotFoundException e) {
//				throw new RuntimeException("This should not happen", e);
//			}
//			String itemValueStr = signatureBeanJson.getRawKeyValues()[i]; 
//			Method valueOfMethod;
//			try {
//				if (itemType == String.class) {
//					valueOfMethod = null;
//				} else {
//					valueOfMethod = itemType.getMethod("valueOf", String.class);					
//				}
//			} catch (NoSuchMethodException e) {
//				throw new RuntimeException("This should not happen", e);
//			} catch (SecurityException e) {
//				throw new RuntimeException("This should not happen", e);
//			}
//			Object itemValue;
//			try {
//				if (valueOfMethod == null) {
//					itemValue = itemValueStr;
//				} else {
//					itemValue = valueOfMethod.invoke(null, itemValueStr);					
//				}
//			} catch (IllegalAccessException e) {
//				throw new RuntimeException("This should not happen", e);
//			} catch (IllegalArgumentException e) {
//				throw new RuntimeException("This should not happen", e);
//			} catch (InvocationTargetException e) {
//				throw new RuntimeException("This should not happen", e);
//			}
//			rawObjValueList.add(itemValue);
//		}
//		signatureBean.setRawKeyValues(rawObjValueList.toArray());
		signatureBean.setStringifiedObjectId(signatureBeanJson.getStringifiedObjectId());
		try {
			signatureBean.setClazz(Class.forName(signatureBeanJson.getClazzName()));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("This should not happen", e);
		}
		signatureBean.setEntityName(signatureBeanJson.getClazzName());
		signatureBean.setPropertyName(signatureBeanJson.getPropertyName());
		signatureBean.setIsColl(signatureBeanJson.getIsColl());
		signatureBean.setIsComp(signatureBeanJson.getIsComp());
		
		signatureBean.setSignature(signatureStr);
		
		return signatureBean;
	}


	@SuppressWarnings("unused")
	@Override
	public SignatureBean generateLazySignature(Collection<?> persistentCollection) {
		AssociationAndComponentTrackInfo aacTrackInfo = this.getCurrentAssociationAndComponentTrackInfo();
//		Object ownerValue = this.getHbSupport().getCollectionOwner(persistentCollection);
		Object ownerValue = aacTrackInfo.getEntityOwner();
		Class<?> ownerClass = ownerValue.getClass();
//		String fieldName = this.getHbSupport().getCollectionFieldName(persistentCollection);
		String fieldName = aacTrackInfo.getEntityAndComponentPath().getAacKey().getPathFromOwner();
		Object fieldValue = persistentCollection;

		SignatureBean signatureBean = this.generateLazySignatureForCollRelashionship(ownerClass, fieldName, ownerValue,
				aacTrackInfo.getEntityAndComponentPath().getAacKey().getPathFromOwner());
		signatureBean.setIsColl(true);
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("generateLazySignature(). signatureBean:\nsignatureBean:\n{0}",
					signatureBean));
		}
		
		return signatureBean;
	}

	@Override
	public SignatureBean generateLazySignatureForCollRelashionship(Class<?> ownerClass, String fieldName,
			Object ownerValue, Object fieldValue) {
		if (!this.getObjPersistenceSupport().isCollectionRelationship(ownerClass, fieldName)) {
			throw new RuntimeException("This is not a relationship: " + ownerClass + "->" + fieldName);
		}
		
		if (!this.getObjPersistenceSupport().isCollectionRelationship(ownerClass, fieldName)) {
			throw new RuntimeException("Unespected type " + ownerClass + "->" + fieldName);
		}

		SignatureBean signatureBean = null;
//		if (ownerValue instanceof HibernateProxy) {
//			signatureBean = this.generateLazySignature((HibernateProxy) ownerValue);
//		} else {
		signatureBean = this.generateSignature(ownerValue);
//		}
		if (ownerValue == null) {
			throw new IllegalArgumentException(
					"ownerValue can not be null em caso de CollectionType: " + ownerClass + "->" + fieldName);
		}
		// if collection is not loaded "collType.getKeyOfOwner" is inconsistent (null)
		// on hb-3
		signatureBean.setClazz(ownerClass);
		signatureBean.setIsColl(true);
		signatureBean.setPropertyName(fieldName);
	
		if (logger.isTraceEnabled()) {
			logger.trace(
					MessageFormat.format("generateLazySignatureForRelashionship().signatureBean:\n{0}", signatureBean));
		}

		return signatureBean;
	}

	@Override
	public SignatureBean generateLazySignatureForLazyProperty(Class<?> ownerClass, String fieldName,
			Object ownerValue, Object fieldValue) {
		if (fieldValue instanceof byte[] || fieldValue instanceof Blob || fieldValue instanceof String
				|| fieldValue instanceof Clob) {
			// nothing
		} else {
			throw new RuntimeException("fieldValue type does not support LazyProperty: " + fieldValue.getClass());
		}

		//if (prpType instanceof AssociationType) {
		if (this.getObjPersistenceSupport().isCollectionRelationship(ownerClass, fieldName)
			|| this.getObjPersistenceSupport().isManyToOneRelationship(ownerClass, fieldName)) {
			throw new RuntimeException("Unespected type " + ownerClass + "->" + fieldName);
		}

		SignatureBean signatureBean = this.generateSignature(ownerValue);
		signatureBean.setIsLazyProperty(true);
		signatureBean.setPropertyName(fieldName);
		
		return signatureBean;
	}

//	@Override
//	public SignatureBean generateLazySignature(Object notLoadObject) {
//		Class<?> entityClass = notLoadObject.getClass().getSuperclass();
//		SignatureBean signatureBean = new SignatureBean();
//		signatureBean.setClazz(entityClass);
//		signatureBean.setEntityName(entityClass.getName());
//		signatureBean.setPropertyName(null);
//
//		//Object[] rawKeyValuesFromHbProxy = this.getObjPersistenceSupport().getRawKeyValuesFromHbProxy(hibernateProxy);
//		String stringfyObjectId = this.getObjPersistenceSupport().stringfyObjectId(this, notLoadObject);
//
//		signatureBean.setStringifiedObjectId(stringfyObjectId);
//		
//		if (logger.isTraceEnabled()) {
//			logger.trace(MessageFormat.format("generateLazySignature(). signatureBean:\nsignatureBean:\n{0}",
//					signatureBean));
//		}
//		
//		return signatureBean;
//	}

	@Override
	public SignatureBean generateComponentSignature(AssociationAndComponentTrackInfo entityAndComponentTrackInfo) {
		SignatureBean signatureBean = this.generateSignature(entityAndComponentTrackInfo.getEntityOwner());
		signatureBean.setPropertyName(
				entityAndComponentTrackInfo.getEntityAndComponentPath().getAacKey().getPathFromOwner());
		signatureBean.setIsComp(true);
		return signatureBean;
	}
	
	@Override
	public SignatureBean generateSignature(Object value) {
//		if (value instanceof HibernateProxy) {
//			throw new RuntimeException("nonHibernateProxy instanceof HibernateProxy: " + value);
//		}
		Class<?> entityClass = value.getClass();
		entityClass = this.config.getObjPersistenceSupport().unwrappRealType(entityClass);
		
		SignatureBean signatureBean = new SignatureBean();
		signatureBean.setClazz(entityClass);
		signatureBean.setEntityName(entityClass.getName());
		signatureBean.setPropertyName(null);
		
		//Object[] rawKeyValuesFromHbProxy = this.getObjPersistenceSupport().getRawKeyValuesFromNonHbProxy(alreadyLoadObject);
		String stringfyObjectId = this.getObjPersistenceSupport().stringfyObjectId(this, value);
		
		signatureBean.setStringifiedObjectId(stringfyObjectId);
		
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("generateSignature(Object value). signatureBean:\n{0}",
					signatureBean));
		}
		
		return signatureBean;
	}

	@Override
	public <T> T getBySignature(SignatureBean signature) {
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("getBySignature(). Begin. \nsignatureBean:\n{0}", signature));
		}
		//Object idValue = this.getObjPersistenceSupport().getIdValue(signature.getClazz(), signature.getRawKeyValues());
		Object idValue = this.getObjPersistenceSupport().parseObjectId(this, signature.getClazz(), signature.getStringifiedObjectId());
		Object owner = this.getObjPersistenceSupport().getById(signature.getClazz(), idValue);
		Object result = owner;

		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("getBySignature(). Hibernate id Type: ''{0}''", idValue.getClass()));
		}
		
		//Type propertyType = null;
		if (signature.getPropertyName() != null) {
			try {
				result = PropertyUtils.getNestedProperty(owner, signature.getPropertyName());
			} catch (Exception e) {
				throw new RuntimeException("This should not happen for property");				
			}
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("getBySignature(). \nsignatureBean:\n{0}\nresult.getClass(): ''{1}''",
					signature, (result != null ? result.getClass().toString() : "null")));
		}

		return (T) result;
	}

//	@SuppressWarnings("rawtypes")
//	@Override
//	public boolean isNeverSigned(Class<?> clazz) {
//		return this.config.getNeverSignedClasses().contains(clazz);
//	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean isPersistentClass(Class<?> clazz) {
		return this.getObjPersistenceSupport().isPersistentClass(clazz);
	}

	@Override
	public Object getPlayerObjectId(Object object) {
		if (logger.isTraceEnabled()) {
			logger.trace("getHibernateObjectId()");
		}
		Class<?> entityClass = object.getClass();
		entityClass = this.config.getObjPersistenceSupport().unwrappRealType(entityClass);
//		if (object instanceof HibernateProxy) {
//			entityClass = object.getClass().getSuperclass();
//		}

		@SuppressWarnings("deprecation")
		Object idValue = this.getObjPersistenceSupport().getIdValue(object);
		return idValue;
	}
	
	@Override
	public String getPlayerObjectIdName(Class clazz) {

		return this.getObjPersistenceSupport().getPlayerObjectIdPrpName(clazz);
	}

	@Override
	public IPlayerConfig getConfig() {
		if (this.temporaryConfigurationTL.get() != null) {
			return this.temporaryConfigurationTL.get();
		} else {
			return this.config;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void startJsonWriteIntersept() {
		if (logger.isTraceEnabled()) {
			logger.trace("startSuperSync()");
		}
		this.validateInited();
		if (this.registeredComponentOwnersTL.get() == null) {
			this.registeredComponentOwnersTL.set(new HashMap<>());
		}
		this.currIdTL.set(0L);
		this.objectByIdMapTL.set(new HashMap<Long, Object>());
		this.idByObjectMapTL.set(new HashMap<IdentityRefKey, Long>());
		this.metadatasCacheMapTL.set(new HashMap<>());
		this.playerBeanPropertyWriterStepStackTL.set(new Stack<PlayerBeanPropertyWriter>());
		this.playerJsonSerializerStepStackTL.set(new Stack<PlayerJsonSerializer>());
		this.playerMetadatasWritingStackTL.set(new Stack<PlayerMetadatas>());
		//this.registeredComponentOwnersTL.set(new HashMap<>());
//		this.currentCompositeOwner.set(null);
//		this.currentCompositePathStackTL.set(new Stack<>());
	}

	@Override
	public void stopJsonWriteIntersept() {
		if (logger.isTraceEnabled()) {
			logger.trace("stopSuperSync()");
		}
		this.currIdTL.set(null);
		this.objectByIdMapTL.set(null);
		this.idByObjectMapTL.set(null);
		this.metadatasCacheMapTL.set(null);
		this.playerBeanPropertyWriterStepStackTL.set(null);
		this.playerJsonSerializerStepStackTL.set(null);
		this.playerMetadatasWritingStackTL.set(null);
//		this.currentCompositeOwner.set(null);
//		this.currentCompositePathStackTL.set(null);
		
		this.temporaryConfigurationTL.set(null);
		this.registeredComponentOwnersTL.set(new HashMap<>());
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
	public Map<IdentityRefKey, PlayerMetadatas> getMetadatasCacheMap() {
		this.validateStarted();
		return this.metadatasCacheMapTL.get();
	}

	@Override
	public Stack<PlayerBeanPropertyWriter> getPlayerBeanPropertyWriterStepStack() {
		return this.playerBeanPropertyWriterStepStackTL.get();
	}
	
	@Override
	public Stack<PlayerJsonSerializer> getPlayerJsonSerializerStepStack() {
		return this.playerJsonSerializerStepStackTL.get();
	}

	@Override
	public Stack<PlayerMetadatas> getPlayerMetadatasWritingStack() {
		return playerMetadatasWritingStackTL.get();
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
	public boolean isComponent(Class<?> componentClass) {
		return this.getObjPersistenceSupport().isComponent(componentClass);
	}

	@Override
	public IPlayerManager overwriteConfigurationTemporarily(IPlayerConfig newConfig) {
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("overwriteConfigurationTemporarily(). newConfig:\n {0}'", newConfig));
		}
		this.temporaryConfigurationTL.set(newConfig);
		return this;
	}
	
	@Override
	public IPlayerManager cloneWithNewConfiguration(IPlayerConfig newConfig) {
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("cloneWithNewConfiguration(). newConfig:\n {0}'", newConfig));
		}
		IPlayerManager managerCloned = new PlayerManagerDefault();
		managerCloned = managerCloned.configure(newConfig);
		return managerCloned;
	}
	
	@Override
	public AssociationAndComponentTrackInfo getCurrentAssociationAndComponentTrackInfo() {
		List<String> pathList = new ArrayList<>();
		Object lastEntityOwner = null;
		Stack<PlayerJsonSerializer> serStepTackLocal = new Stack<>();
		serStepTackLocal.addAll(this.playerJsonSerializerStepStackTL.get());
		// ignoring last one
		serStepTackLocal.pop();
		for (PlayerJsonSerializer playerJsonSerializer : serStepTackLocal) {
			Object currBean = playerJsonSerializer.getCurrSerializationBean();
			if (currBean instanceof PlayerMetadatas) {
				currBean = ((PlayerMetadatas) currBean).getOriginalPlayerObjectIdOwner();
			}
			if (playerJsonSerializer.getCurrSerializationBean() != null && this.isPersistentClass(currBean.getClass())) {
				lastEntityOwner = currBean;
			}
		}

		for (PlayerBeanPropertyWriter jbHbBeanPropertyWriter : this.getPlayerBeanPropertyWriterStepStack()) {
			if (jbHbBeanPropertyWriter.getCurrOwner() != null 
					&& this.isPersistentClass(jbHbBeanPropertyWriter.getCurrOwner().getClass())) {
				lastEntityOwner = jbHbBeanPropertyWriter.getCurrOwner();
				pathList.clear();
				pathList.add(jbHbBeanPropertyWriter.getBeanPropertyDefinition().getInternalName());
			} else {
				pathList.add(jbHbBeanPropertyWriter.getBeanPropertyDefinition().getInternalName());					
			}
		}
		if (lastEntityOwner != null && pathList.size() > 0) {
			String pathStr = this.mountPathFromStack(pathList);
//			AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(lastEntityOwner.getClass(),
//					pathStr);
			if (this.getObjPersistenceSupport().isComponentOrRelationship(lastEntityOwner.getClass(), pathStr)) {
				AssociationAndComponentTrackInfo trackInfo = new AssociationAndComponentTrackInfo();
				trackInfo.setEntityOwner(lastEntityOwner);
				trackInfo.setEntityAndComponentPath(this.getObjPersistenceSupport().getAssociationAndComponentOnPath(lastEntityOwner.getClass(), pathStr));
				return trackInfo;
			} else {
				return null;
			}
		} else {
			return null;			
		}
	}

	@Override
	public String getCurrentPathFromLastEntity() {
		List<String> pathList = new ArrayList<>();
		Object lastEntityOwner = null;
		for (PlayerBeanPropertyWriter jbHbBeanPropertyWriter : this.getPlayerBeanPropertyWriterStepStack()) {
			if (this.isPersistentClass(jbHbBeanPropertyWriter.getCurrOwner().getClass())) {
				lastEntityOwner = jbHbBeanPropertyWriter.getCurrOwner();
				pathList.clear();
				pathList.add(jbHbBeanPropertyWriter.getBeanPropertyDefinition().getInternalName());
			} else {
				pathList.add(jbHbBeanPropertyWriter.getBeanPropertyDefinition().getInternalName());
			}
		}
		String pathStr = null;
		if (lastEntityOwner != null && pathList.size() > 0) {
			pathStr = this.mountPathFromStack(pathList);
		}
		return pathStr;
	}

	@Override
	public boolean isCurrentPathFromLastEntityAnEntityRelationship() {
		List<String> pathList = new ArrayList<>();
		Object lastEntityOwner = null;
		for (PlayerBeanPropertyWriter jbHbBeanPropertyWriter : this.getPlayerBeanPropertyWriterStepStack()) {
			if (this.isPersistentClass(jbHbBeanPropertyWriter.getCurrOwner().getClass())) {
				lastEntityOwner = jbHbBeanPropertyWriter.getCurrOwner();
				pathList.clear();
				pathList.add(jbHbBeanPropertyWriter.getBeanPropertyDefinition().getInternalName());
			} else {
				pathList.add(jbHbBeanPropertyWriter.getBeanPropertyDefinition().getInternalName());
			}
		}
		String pathStr = null;
		if (lastEntityOwner != null && pathList.size() > 0) {
			pathStr = this.mountPathFromStack(pathList);
			if (this.getObjPersistenceSupport().isManyToOneRelationship(lastEntityOwner.getClass(), pathStr)) {
				return true;
			}
//			AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(lastEntityOwner.getClass(),
//					pathStr);
//			AssociationAndComponentPath entityAndComponentPath = this.associationAndCompositiesMap.get(aacKey);
//			if (entityAndComponentPath != null) {
//				if (entityAndComponentPath.getRelEntity() != null) {
//					return true;
//				}
//			}
		}
		return false;
	}

	protected String mountPathFromStack(String[] pathStack) {
		return mountPathFromStack(Arrays.asList(pathStack));
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
	
	@Override
	public IDirectRawWriterWrapper needDirectWrite(SignatureBean signature) {
		JsonSerializer<Object> jsonSerializer = null;
		try {
			jsonSerializer = this.config.getObjectMapper().getSerializerProvider()
					.findValueSerializer(signature.getClazz());
		} catch (JsonMappingException e) {
			throw new RuntimeException("This should not happen", e);
		}
		if (jsonSerializer instanceof BeanSerializer) {
			BeanSerializer beanSerializer = (BeanSerializer) jsonSerializer;
			Iterator<PropertyWriter> iterator = beanSerializer.properties();
			final Object objectToRawWrite = this.getBySignature(signature);
			while (iterator.hasNext()) {
				PropertyWriter propertyWriter = (PropertyWriter) iterator.next();
				if (propertyWriter instanceof PlayerBeanPropertyWriter) {
					PlayerBeanPropertyWriter playerBeanPropertyWriter = (PlayerBeanPropertyWriter) propertyWriter;
					if (playerBeanPropertyWriter.getBeanPropertyDefinition().getInternalName()
							.equals(signature.getPropertyName())) {
						final LazyProperty lazyPropertyAnn = playerBeanPropertyWriter
								.getAnnotation(LazyProperty.class);
						if (lazyPropertyAnn != null && lazyPropertyAnn.directRawWrite()) {
							return new IDirectRawWriterWrapper() {

								@Override
								public LazyProperty getLazyProperty() {
									return lazyPropertyAnn;
								}

								@Override
								public IDirectRawWriter getCallback() {
									return new IDirectRawWriter() {

										@Override
										public void write(OutputStream outputStream) throws IOException, SQLException {
											if (objectToRawWrite == null) {
												// nothing
											} else if (objectToRawWrite instanceof Blob) {
												Blob blob = (Blob) objectToRawWrite;
												byte[] buffer = new byte[lazyPropertyAnn.bufferSize()];
												int offset = 0;
												int latReadedCount = 0;
												InputStream inputStream = blob.getBinaryStream();
												do {
													latReadedCount = inputStream.read(buffer, offset, buffer.length);
													offset = offset + latReadedCount;
													if (latReadedCount > 0) {
														outputStream.write(buffer, 0, latReadedCount);
													}
												} while (latReadedCount > 0);
												outputStream.flush();

											} else if (objectToRawWrite instanceof Clob) {
												Clob clob = (Clob) objectToRawWrite;
												int latReadedCount = 0;
												Charset charset = Charset.forName(lazyPropertyAnn.charset());
												CharBuffer charBuffer = CharBuffer
														.allocate(lazyPropertyAnn.bufferSize());

												Reader r = clob.getCharacterStream();
												WritableByteChannel channel = Channels.newChannel(outputStream);
												do {
													latReadedCount = r.read(charBuffer);
													if (latReadedCount > 0) {
														channel.write(charset.encode(charBuffer));
													}
												} while (latReadedCount > 0);
												outputStream.flush();
											} else if (objectToRawWrite instanceof byte[]) {
												byte[] byteArr = (byte[]) objectToRawWrite;
												outputStream.write(byteArr, 0, byteArr.length);
												outputStream.flush();
											} else if (objectToRawWrite instanceof String) {
												String str = (String) objectToRawWrite;
												Charset charset = Charset.forName(lazyPropertyAnn.charset());
												WritableByteChannel channel = Channels.newChannel(outputStream);
												channel.write(charset.encode(str));
												outputStream.flush();
											} else {
												throw new RuntimeException(
														"Only Blob, Clob, byte[] or String supported by 'direct OutputStream write. Not supported type: "
																+ objectToRawWrite.getClass().getName());
											}
										}
									};
								}
							};
						}
					}
				}
			}
		} else {
			throw new RuntimeException("This should not happen");
		}
		return null;
	}

	private <O> void registerComponentOwnerPriv(O owner, String propertyName, Object owned) {
		if (owned == null) {
			throw new RuntimeException("propertyFunc can not return null!");
		}
		IdentityRefKey identityRefKey = new IdentityRefKey(owned);
		Class<O> ownerClass = (Class<O>) owner.getClass();
		ownerClass = (Class<O>) this.config.getObjPersistenceSupport().unwrappRealType(ownerClass);
//		if (owner instanceof HibernateProxy) {
//			ownerClass = (Class<O>) owner.getClass().getSuperclass();
//		} else {
//			ownerClass = (Class<O>) owner.getClass();
//		}
		OwnerAndProperty ownerAndProperty = new OwnerAndProperty();
		ownerAndProperty.setOwner(owner);
		ownerAndProperty.setProperty(propertyName);
		this.registeredComponentOwnersTL.get().put(identityRefKey, ownerAndProperty);
	}
	
	/**
	 * Here you can NOT use nested properties.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <O> IPlayerManager registerComponentOwner(O owner, Function<O, ?> propertyFunc) {
		Object owned = propertyFunc.apply(owner);
		Class<O> ownerClass = (Class<O>) owner.getClass();
		ownerClass = (Class<O>) this.config.getObjPersistenceSupport().unwrappRealType(ownerClass);
//		if (owner instanceof HibernateProxy) {
//			ownerClass = (Class<O>) owner.getClass().getSuperclass();
//		} else {
//			ownerClass = (Class<O>) owner.getClass();
//		}
		String propertyName = ReflectionNamesDiscovery.fieldByGetMethod(propertyFunc, ownerClass);
		this.registerComponentOwnerPriv(owner, propertyName, owned);
		return this;
	}

	/**
	 * Here you can use nested properties.
	 */
	@Override
	public <O, T> IPlayerManager registerComponentOwner(Class<O> ownerClass, T targetOwned, Function<O, T> propertyFunc) {
		List<PathEntry> pathEntries = ReflectionNamesDiscovery.fieldByGetMethodEntries(propertyFunc, ownerClass);
		Object ownerInst = null;
		for (int i = 0; i < pathEntries.size(); i++) {
			PathEntry pathEntry = pathEntries.get(i);
			try {
				if (ownerInst == null) {
					ownerInst = pathEntry.getDirectOwnerType().newInstance();
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("pathEntry.getDirectOwnerType().newInstance() fail for " + pathEntry.getDirectOwnerType());
			}
			Object ownedInst = null; 
			if (i < pathEntries.size() - 1) {
				try {
					ownedInst = pathEntry.getDirectFieldType().newInstance();
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException("pathEntry.getDirectOwnerType().newInstance() fail for " + pathEntry.getDirectOwnerType());
				}
			} else {
				ownedInst = targetOwned;
			}
			try {
				PropertyUtils.setProperty(ownerInst, pathEntry.getDirectFieldName(), ownedInst);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("could not set field " + pathEntry.getDirectFieldName() + " for " + pathEntry.getDirectOwnerType());
			}
			this.registerComponentOwnerPriv(ownerInst, pathEntry.getDirectFieldName(), ownedInst);
			ownerInst = ownedInst;
		}
		return this;
	}
	
	@Override
	public List<OwnerAndProperty> getRegisteredComponentOwnerList(Object instance) {
		this.validateInited();
		ArrayList<OwnerAndProperty> resultList = new ArrayList<>();
		OwnerAndProperty currOwnerAndProperty = null;
		IdentityRefKey currIdentityRefKey = new IdentityRefKey(instance);
		do {
			currOwnerAndProperty = this.registeredComponentOwnersTL.get().get(currIdentityRefKey);				
			if (currOwnerAndProperty != null) {
				resultList.add(currOwnerAndProperty);
				currIdentityRefKey = new IdentityRefKey(currOwnerAndProperty.getOwner());
			} else {
				currIdentityRefKey = new IdentityRefKey(new Object());
			}
		} while (currOwnerAndProperty != null);
		Collections.reverse(resultList);
		return resultList;
	}	
	private ObjPersistenceSupport objPersistenceSupport;
	public ObjPersistenceSupport getObjPersistenceSupport() {
		return this.objPersistenceSupport;
	}
	
	private ObjPersistenceMode objPersistenceMode;
	public ObjPersistenceMode getObjPersistenceMode() {
		return this.objPersistenceMode;
	}
	
	public static ObjPersistenceMode getObjPersistenceModeStatic() {
		return ObjPersistenceMode.valueOf(envPrps.getProperty(OBJ_PERSISTENCE_MODE));
	}

	@Override
	public boolean isManagerOfSignature(String signature) {
		this.validateInited();
		int indexOfFlag = signature.indexOf(this.config.getManagerIdSignaturePrefixFlag());
		if (indexOfFlag >= 0) {
			String possibleId = signature.substring(0, indexOfFlag);
			return this.config.getManagerId().equals(possibleId);
		} else {
			return false;
		}
	}
	
	private Map<String, Class<?>> managedTypeMap;
	@Override
	public boolean isManagerOfType(String typeNameOrAlias) {
		this.validateInited();
		return this.managedTypeMap.containsKey(typeNameOrAlias);
	}

	@Override
	public boolean isManagerOfType(Class<?> type) {
		this.validateInited();
		Class<?> unwrappedType = this.config.getObjPersistenceSupport().unwrappRealType(type);
		return this.isManagerOfType(unwrappedType.getName());
	}

	@Override
	public Class<?> resolveType(String typeNameOrAlias) {
		this.validateInited();
		return this.managedTypeMap.get(typeNameOrAlias);
	}
	
	private void validateInited() {
		if (!this.initialed) {
			throw new RuntimeException("Manager is not inited. Call IPlayerManager.init()");	
		}
	}
}