package org.jsonplayback.player;

import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface IPlayerConfig {
	List<IGetBySignatureListener> getListeners();

	//SessionFactory getSessionFactory();
	ObjPersistenceSupport getObjPersistenceSupport();

	SignatureCrypto getSignatureCrypto();

	Set<Class> getNeverSignedClasses();

	IPlayerConfig configNeverSignedClasses(Set<Class> getNotLazyClasses);

	IPlayerConfig configObjPersistenceSupport(ObjPersistenceSupport objPersistenceSupport);

	IPlayerConfig configSerialiseBySignatureAllRelationship(boolean serialiseBySignatureAllRelationship);

	boolean isSerialiseBySignatureAllRelationship();
	
	IPlayerConfig clone();

	ObjectMapper getObjectMapper();

	IPlayerConfig configObjectMapper(ObjectMapper objectMapper);

	Set<Class> getNonLazybleClasses();

	String getPlayerMetadatasName();

	IPlayerConfig configPlayerMetadatasName(String playerMetadatasName);

	boolean isIgnoreAllLazyProperty();

	IPlayerConfig configIgnoreAllLazyProperty(boolean ignoreAllLazyProperty);
}
/*gerando conflito*/