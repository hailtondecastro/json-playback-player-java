package org.jsonplayback.player.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.jsonplayback.player.IGetBySignatureListener;
import org.jsonplayback.player.IPlayerConfig;
import org.jsonplayback.player.ObjPersistenceSupport;
import org.jsonplayback.player.SignatureCrypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private BasicClassIntrospector basicClassIntrospector = new PlayerBasicClassIntrospector();
	
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
}
/* gerando conflito */