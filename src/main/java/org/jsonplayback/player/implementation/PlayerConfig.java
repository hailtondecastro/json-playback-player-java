package org.jsonplayback.player.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.jsonplayback.player.IGetBySignatureListener;
import org.jsonplayback.player.IPlayerConfig;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.ObjPersistenceSupport;
import org.jsonplayback.player.PlayerMetadatas;
import org.jsonplayback.player.SignatureCrypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;

public class PlayerConfig implements IPlayerConfig, Cloneable {
	private String playerMetadatasName = "$metadatas$";
	private Set<Class> neverSignedClasses = new HashSet<>();
	private Set<Class> nonLazybleClasses = new HashSet<>();
	private List<IGetBySignatureListener> listeners = new ArrayList<>();
	private ObjPersistenceSupport objPersistenceSupport;
	private ObjectMapper objectMapper;
	private SignatureCrypto signatureCrypto;
	private boolean serialiseBySignatureAllRelationship = false;
	private boolean ignoreAllLazyProperty = false;
	private BasicClassIntrospector basicClassIntrospector = new BasicClassIntrospector() {
		public BasicBeanDescription forSerialization(SerializationConfig cfg, JavaType type, MixInResolver r) {
			Class<?> unwrappedType = PlayerConfig.this.getObjPersistenceSupport().unwrappRealType(type.getRawClass());
			JavaType unwrappedTypeJackon = PlayerConfig.this.objectMapper.getTypeFactory().constructType(unwrappedType);
			return super.forSerialization(cfg, unwrappedTypeJackon, r);
		}; 
	};
	private char managerIdSignaturePrefixFlag = '.';
	private String managerId = null;
	
	private Set<String> addictionalManagedTypeSet = new HashSet<String>();
	
	private Function<IPlayerManager, PlayerMetadatas> metadataInstantiator =
			(manager) -> {
				return new PlayerMetadatas(manager);
			};
	
	@Override
	public Function<IPlayerManager, PlayerMetadatas> getMetadataInstantiator() {
		return metadataInstantiator;
	}

	@Override
	public IPlayerConfig configMetadataInstantiator(Function<IPlayerManager, PlayerMetadatas> metadataInstantiator) {
		this.metadataInstantiator = metadataInstantiator;
		return this;
	}

	@Override
	public char getManagerIdSignaturePrefixFlag() {
		return managerIdSignaturePrefixFlag;
	}

	@Override
	public IPlayerConfig configManagerIdSignaturePrefixFlag(char managerIdSignaturePrefixFlag) {
		this.managerIdSignaturePrefixFlag = managerIdSignaturePrefixFlag;
		return this;
	}

	@Override
	public String getManagerId() {
		if(this.managerId == null) {
			throw new RuntimeException("managerId can not be null");
		}
		return managerId;
	}

	@Override
	public IPlayerConfig configManagerId(String managerId) {
		this.managerId = managerId;
		return this;
	}

	@Override
	public BasicClassIntrospector getBasicClassIntrospector() {
		return basicClassIntrospector;
	}

	@Override
	public IPlayerConfig configBasicClassIntrospector(BasicClassIntrospector basicClassIntrospector) {
		this.basicClassIntrospector = basicClassIntrospector;
		return this;
	}

	@Override
	public boolean isIgnoreAllLazyProperty() {
		return ignoreAllLazyProperty;
	}

	@Override
	public IPlayerConfig configIgnoreAllLazyProperty(boolean ignoreAllLazyProperty) {
		this.ignoreAllLazyProperty = ignoreAllLazyProperty;
		return this;
	}

	@Override
	public String getPlayerMetadatasName() {
		return playerMetadatasName;
	}

	@Override
	public IPlayerConfig configPlayerMetadatasName(String playerMetadatasName) {
		this.playerMetadatasName = playerMetadatasName;
		return this;
	}

	@Override
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	@Override
	public IPlayerConfig configObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		return this;
	}

	@Override
	public boolean isSerialiseBySignatureAllRelationship() {
		return serialiseBySignatureAllRelationship;
	}

	@Override
	public IPlayerConfig configSerialiseBySignatureAllRelationship(boolean serialiseBySignatureAllRelationship) {
		this.serialiseBySignatureAllRelationship = serialiseBySignatureAllRelationship;
		return this;
	}

	@Override
	public IPlayerConfig configNeverSignedClasses(Set<Class> neverSignedClasses) {
		//this.neverSignedClasses = neverSignedClasses;
		return this;
	}


	@Override
	public IPlayerConfig configObjPersistenceSupport(ObjPersistenceSupport objPersistenceSupport) {
		this.objPersistenceSupport = objPersistenceSupport;
		return this;
	}

	@Override
	public List<IGetBySignatureListener> getListeners() {
		return Collections.unmodifiableList(this.listeners);
	}

	@Override
	public ObjPersistenceSupport getObjPersistenceSupport() {
		return this.objPersistenceSupport;
	}

	@Override
	public SignatureCrypto getSignatureCrypto() {
		return this.signatureCrypto;
	}

	@Override
	public Set<Class> getNeverSignedClasses() {
		return this.neverSignedClasses;
	}

	@Override
	public Set<Class> getNonLazybleClasses() {
		return nonLazybleClasses;
	}

	@Override
	public PlayerConfig clone() {
		try {
			return (PlayerConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
	
	
	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LinkedHashMap<String, Object> thisAsMap = new LinkedHashMap<>();
			ArrayList<String> listenersList = new ArrayList<>();
			for (IGetBySignatureListener listenerItem : this.getListeners()) {
				listenersList.add(listenerItem.getName());
			}
			thisAsMap.put("serialiseBySignatureAllRelationship", this.isSerialiseBySignatureAllRelationship());
			thisAsMap.put("listeners", listenersList);
			thisAsMap.put("sessionFactory", this.getObjPersistenceSupport() != null? this.getObjPersistenceSupport().getClass(): "null");
			thisAsMap.put("signatureCrypto", this.getSignatureCrypto()!= null? this.getSignatureCrypto().getClass(): "null");
			//thisAsMap.put("neverSignedClasses", this.getNeverSignedClasses());
			thisAsMap.put("playerMetadatasName", this.getPlayerMetadatasName());
			thisAsMap.put("nonLazybleClasses", this.getNonLazybleClasses());
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(thisAsMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
	
	@Override
	public IPlayerConfig registerAddictionalManagedType(Class<?> type) {
		this.addictionalManagedTypeSet.add(type.getName());
		return this;
	}
	
	@Override
	public Set<Class> addictionalManagedTypes() {
		LinkedHashSet<Class> result = new LinkedHashSet<>();
		for (String typeStr : this.addictionalManagedTypeSet) {
			try {
				result.add(Class.forName(typeStr));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("This should not happen", e);
			}
		}
		return result;
	}
}
/* gerando conflito */