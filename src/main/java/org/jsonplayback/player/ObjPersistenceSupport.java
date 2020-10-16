package org.jsonplayback.player;

import java.util.Set;

import org.jsonplayback.hibernate.CollectionStyle;
import org.jsonplayback.player.implementation.AssociationAndComponentPath;
import org.jsonplayback.player.implementation.AssociationAndComponentTrackInfo;
import org.jsonplayback.player.implementation.IPlayerManagerImplementor;

public interface ObjPersistenceSupport {
	//boolean isPersistentCollection(AssociationAndComponentTrackInfo aacTrackInfo, Object coll);
	boolean isLazyUnitialized(Object coll, Object rootOwner, String pathFromOwner);
	boolean isLazyUnitialized(Object entity);
//	Connection getConnection();
//	Object getCollectionOwner(Object coll);
//	String getCollectionFieldName(Object coll);
	String getCollectionGetRole(Object coll);
//	Object[] getRawKeyValuesFromHbProxy(Object hibernateProxy);
//	Object[] getRawKeyValuesFromNonHbProxy(Object nonHibernateProxy);
	void collectAssociationAndCompositiesMap();
	void init(IPlayerManagerImplementor playerManagerImplementor);
	boolean isComponent(Class<?> componentClass);
	boolean isPersistentClass(Class<?> clazz);
	boolean isCollectionRelationship(Class<?> ownerClass, String pathFromOwner);
	boolean isManyToOneRelationship(Class<?> ownerClass, String pathFromOwner);
	boolean isComponentOrRelationship(Class<?> ownerClass, String pathFromOwner);
	boolean isComponentByTrack(AssociationAndComponentTrackInfo aacTrackInfo);
	//Object getIdValue(Class<?> entityClass, Object[] rawKeyValues);
	Object getIdValue(Object entityInstanceOrProxy);
	Object getById(Class<?> entityClass, Object idValue);
	AssociationAndComponentPath getAssociationAndComponentOnPath(Class<?> ownerClass, String pathStr);
	boolean testCollectionStyle(Class<?> ownerClass, String prpName, CollectionStyle style);
//	<R> CriteriaCompat<R> createCriteria(Session session, Class<R> clazz);
//	<R> CriteriaCompat<R> createCriteria(EntityManager em, Class<R> clazz);
	void processNewInstantiate(Class<?> instType, Object instValue);
	String getPlayerObjectIdPrpName(Class<?> clazz);
	
	void persistenceRemove(Object entity);
	void persistencePersist(Object entity);
	
	String stringfyObjectId(IPlayerManager manager, Object owner);
	Object parseObjectId(IPlayerManager manager, Class<?> ownerClass, String stringifiedObjectId);
	
	Class<?> unwrappRealType(Object possibleWrappedValue);
	Set<Class<?>> allManagedTypes();
	Class<?> unwrappRealType(Class<?> possibleWrapperType);	
}
