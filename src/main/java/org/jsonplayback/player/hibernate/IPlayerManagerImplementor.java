package org.jsonplayback.player.hibernate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.hibernate.proxy.HibernateProxy;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.IdentityRefKey;
import org.jsonplayback.player.ObjPersistenceSupport;
import org.jsonplayback.player.PlayerMetadatas;
import org.jsonplayback.player.SignatureBean;

public interface IPlayerManagerImplementor extends IPlayerManager {
	SignatureBean generateLazySignature(Collection<?> persistentCollection);

	SignatureBean generateLazySignature(HibernateProxy hibernateProxy);

	SignatureBean generateSignature(Object nonHibernateProxy);
	
	Long getCurrId();

	Map<Long, Object> getObjectByIdMap();

	/**
	 * Internal use.
	 * @return
	 */
	Map<IdentityRefKey, Long> getIdByObjectMap();
	
	void currIdPlusPlus();
	
	SignatureBean generateLazySignatureForCollRelashionship(Class<?> ownerClass, String fieldName, Object ownerValue,
			Object fieldValue);
	
	boolean isComponent(Class<?> componentClass);
	
	boolean isPersistentClass(Class clazz);

	Stack<PlayerBeanPropertyWriter> getPlayerBeanPropertyWriterStepStack();

//	Stack<String> getCurrentCompositePathStack();
//	Object getCurrentCompositeOwner();
	
	AssociationAndComponentTrackInfo getCurrentAssociationAndComponentTrackInfo();

	String getPlayerObjectIdName(Class clazz);

	Stack<PlayerJsonSerializer> getPlayerJsonSerializerStepStack();
	
	SignatureBean generateComponentSignature(AssociationAndComponentTrackInfo entityAndComponentTrackInfo);

	Stack<PlayerMetadatas> getPlayerMetadatasWritingStack();

	SignatureBean generateLazySignatureForLazyProperty(Class<?> ownerClass, String fieldName, Object ownerValue,
			Object fieldValue);
	
	String getCurrentPathFromLastEntity();

	boolean isCurrentPathFromLastEntityAnEntityRelationship();

	Map<IdentityRefKey, PlayerMetadatas> getMetadatasCacheMap();
	
	List<OwnerAndProperty> getRegisteredComponentOwnerList(Object instance);
	
	public ObjPersistenceSupport getObjPersistenceSupport();
}
