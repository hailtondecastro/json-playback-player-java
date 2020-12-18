package org.jsonplayback.player;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

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
	
	IPlayerConfig registerAddictionalManagedType(Class<?> type);
	Set<Class> addictionalManagedTypes();

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
	
	/**
	 * Flag used to identify the manager on {@link IPlayerManager#serializeSignature(SignatureBean)}<br>
	 * Default: "."
	 * @param managerIdSignaturePrefixFlag
	 * @return
	 */
	IPlayerConfig configManagerIdSignaturePrefixFlag(char managerIdSignaturePrefixFlag);
	
	/**
	 * Id used to identify the manager by object signature.
	 * @param managerId
	 * @return
	 */
	IPlayerConfig configManagerId(String managerId);

	char getManagerIdSignaturePrefixFlag();

	String getManagerId();

	Function<IPlayerManager, PlayerMetadatas> getMetadataInstantiator();

	IPlayerConfig configMetadataInstantiator(Function<IPlayerManager, PlayerMetadatas> metadataInstantiator);
	
	ObjPersistenceMode getObjPersistenceMode();

	IPlayerConfig configObjPersistenceMode(ObjPersistenceMode objPersistenceMode);
}
/*gerando conflito*/