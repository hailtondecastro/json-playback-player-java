package org.jsonplayback.player;

import java.sql.Connection;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.jsonplayback.hbsupport.CollectionStyle;
import org.jsonplayback.hbsupport.CriteriaCompat;
import org.jsonplayback.player.hibernate.AssociationAndComponentPath;
import org.jsonplayback.player.hibernate.AssociationAndComponentTrackInfo;

public interface ObjPersistenceSupport {
	boolean isPersistentCollection(Object coll);
	boolean isCollectionLazyUnitialized(Object coll, Object rootOwner, String pathFromOwner);
	boolean isHibernateProxyLazyUnitialized(Object hProxy);
	Connection getConnection();
//	Object getCollectionOwner(Object coll);
//	String getCollectionFieldName(Object coll);
	String getCollectionGetRole(Object coll);
	Object[] getRawKeyValuesFromHbProxy(Object hibernateProxy);
	Object[] getRawKeyValuesFromNonHbProxy(Object nonHibernateProxy);
	void collectAssociationAndCompositiesMap();
	void init();
	boolean isComponent(Class<?> componentClass);
	boolean isPersistentClass(Class<?> clazz);
	boolean isCollectionRelationship(Class<?> ownerClass, String pathFromOwner);
	boolean isManyToOneRelationship(Class<?> ownerClass, String pathFromOwner);
	boolean isComponentOrRelationship(Class<?> ownerClass, String pathFromOwner);
	boolean isComponentByTrack(AssociationAndComponentTrackInfo aacTrackInfo);
	Object getIdValue(Class<?> entityClass, Object[] rawKeyValues);
	Object getIdValue(Object entityInstanceOrProxy);
	Object getById(Class<?> entityClass, Object idValue);
	AssociationAndComponentPath getAssociationAndComponentOnPath(Class<?> ownerClass, String pathStr);
	boolean testCollectionStyle(Class<?> ownerClass, String prpName, CollectionStyle style);
	<R> CriteriaCompat<R> createCriteria(Session session, Class<R> clazz);
	<R> CriteriaCompat<R> createCriteria(EntityManager em, Class<R> clazz);
	void processNewInstantiate(Class<?> instType, Object instValue);
	String getPlayerObjectIdPrpName(Class clazz);
}
