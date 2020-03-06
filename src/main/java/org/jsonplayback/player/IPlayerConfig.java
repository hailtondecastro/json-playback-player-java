package org.jsonplayback.player;

import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;

public interface IPlayerConfig {
	List<IGetBySignatureListener> getListeners();

	//SessionFactory getSessionFactory();
	ObjPersistenceSupport getObjPersistenceSupport();

	SignatureCrypto getSignatureCrypto();

	Set<Class> getNeverSignedClasses();

	IPlayerConfig configNeverSignedClasses(Set<Class> getNotLazyClasses);

	IPlayerConfig configObjPersistenceSupport(ObjPersistenceSupport objPersistenceSupport);

	IPlayerConfig configSerialiseBySignatureAllRelationship(boolean serialiseBySignatureAllRelationship);
	
	IPlayerConfig configBasicClassIntrospector(BasicClassIntrospector basicClassIntrospector);

	boolean isSerialiseBySignatureAllRelationship();
	
	IPlayerConfig clone();

	ObjectMapper getObjectMapper();

	IPlayerConfig configObjectMapper(ObjectMapper objectMapper);

	Set<Class> getNonLazybleClasses();

	String getPlayerMetadatasName();
	
	BasicClassIntrospector getBasicClassIntrospector();

	IPlayerConfig configPlayerMetadatasName(String playerMetadatasName);

	boolean isIgnoreAllLazyProperty();

	IPlayerConfig configIgnoreAllLazyProperty(boolean ignoreAllLazyProperty);
}
/*gerando conflito*/