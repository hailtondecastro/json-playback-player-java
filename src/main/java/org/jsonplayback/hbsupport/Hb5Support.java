package org.jsonplayback.hbsupport;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
//import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
//import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.hibernate.AssociationAndComponentPathKey;
import org.jsonplayback.player.hibernate.PlayerResultSet;
import org.jsonplayback.player.hibernate.PlayerStatment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hb5Support extends HbObjPersistenceSupportBase {
	private static Logger logger = LoggerFactory.getLogger(Hb5Support.class);

	private Map<AssociationAndComponentPathKey, AssociationAndComponentPathObjPersistenceSupport> associationAndCompositiesMap = new HashMap<>();
	private Set<Class<?>> compositiesSet = new HashSet<>();

	public Hb5Support(IPlayerManager manager) {
		super(manager);
	}

	@Override
	public boolean isCollectionLazyUnitialized(Object coll, Object rootOwner, String pathFromOwner) {
		return !((boolean)super.runByReflection(
				super.getPersistentCollecitonClass().getName(),
				"wasInitialized",
				new String[]{},
				coll,
				new Object[]{}));
		//return !((org.hibernate.collection.PersistentCollection)coll).wasInitialized();
	}

//	@Override
//	public Object getCollectionOwner(Object coll) {
//		return super.runByReflection(
//				super.getPersistentCollecitonClass().getName(),
//				"getOwner",
//				new String[]{},
//				coll,
//				new Object[]{});
//		//return ((org.hibernate.collection.PersistentCollection)coll).getOwner();
//	}
//	
	@Override
	public Class<?> resolvePersistentCollectionClass() {
		try {
			return Class.forName("org.hibernate.collection.spi.PersistentCollection");
		} catch (Throwable e) {
			throw new RuntimeException("This should not happen", e);
		}
	}
	
	@Override
	public Object[] getRawKeyValuesFromHbProxy(Object hibernateProxy) {
		Class entityClass = hibernateProxy.getClass().getSuperclass();

		ClassMetadata classMetadata = this.getAllClassMetadata().get(entityClass.getName());
		PlayerStatment playerStatment = new PlayerStatment();

		Object idValue = this.getIdValue(hibernateProxy);
//				classMetadata.getIdentifier(hibernateProxy,
//						(org.hibernate.engine.SessionImplementor) this.manager.getConfig().getSessionFactory()
//						.getCurrentSession());
		Type hbIdType = classMetadata.getIdentifierType();
		try {
			this.runByReflection(
				hbIdType.getClass().getName(),
				"nullSafeSet",
				new String[]{ 
					PreparedStatement.class.getName(),
					Object.class.getName(),
					int.class.getName(),
					"org.hibernate.engine.spi.SharedSessionContractImplementor"
				},
				hbIdType,
				new Object[]{
					playerStatment, 
					idValue, 
					0, 
					this.manager.getConfig().getSessionFactory().getCurrentSession()
				}
			);
//			hbIdType.nullSafeSet(playerStatment, idValue, 0,
//					(org.hibernate.engine.spi.SessionImplementor) this.manager.getConfig().getSessionFactory().getCurrentSession());
		} catch (HibernateException e) {
			throw new RuntimeException("This should not happen", e);
		}
		return playerStatment.getInternalValues();
	}

	@Override
	public Object[] getRawKeyValuesFromNonHbProxy(Object nonHibernateProxy) {
		if (nonHibernateProxy instanceof HibernateProxy) {
			throw new RuntimeException("nonHibernateProxy instanceof HibernateProxy: " + nonHibernateProxy);
		}
		Class entityClass = nonHibernateProxy.getClass();
		ClassMetadata classMetadata = this.getAllClassMetadata().get(entityClass.getName());
		PlayerStatment playerStatment = new PlayerStatment();
		
		Object idValue = 
				this.runByReflection(
					classMetadata.getClass().getName(),
					"getIdentifier",
					new String[]{
						Object.class.getName(),
						"org.hibernate.engine.spi.SharedSessionContractImplementor"
					},
					classMetadata,
					new Object[]{
						nonHibernateProxy, 
						this.manager.getConfig().getSessionFactory().getCurrentSession()
					});
//		Object idValue = classMetadata.getIdentifier(nonHibernateProxy,
//				(org.hibernate.engine.spi.SessionImplementor) this.manager.getConfig().getSessionFactory()
//						.getCurrentSession());
		
		Type hbIdType = classMetadata.getIdentifierType();
		try {
			this.runByReflection(
				hbIdType.getClass().getName(),
				"nullSafeSet",
				new String[]{ 
					PreparedStatement.class.getName(),
					Object.class.getName(),
					int.class.getName(),
					"org.hibernate.engine.spi.SharedSessionContractImplementor"
				},
				hbIdType,
				new Object[]{
					playerStatment, 
					idValue, 
					0, 
					this.manager.getConfig().getSessionFactory().getCurrentSession()
				}
			);
//			hbIdType.nullSafeSet(playerStatment, idValue, 0,
//					(org.hibernate.engine.spi.SessionImplementor) this.manager.getConfig().getSessionFactory().getCurrentSession());
		} catch (HibernateException e) {
			throw new RuntimeException("This should not happen", e);
		}
		return playerStatment.getInternalValues();
	}

	@Override
	public String getCollectionGetRole(Object coll) {
		return (String) super.runByReflection(
				super.getPersistentCollecitonClass().getName(),
				"getRole",
				new String[]{},
				coll,
				new Object[]{});
		//return ((org.hibernate.collection.spi.PersistentCollection)coll).getRole();
	}

	@Override
	public Serializable getIdValue(Class<?> entityClass, Object[] rawKeyValues) {
		ClassMetadata classMetadata = this.getAllClassMetadata().get(entityClass.getName());

		Type hbIdType = classMetadata.getIdentifierType();

		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("getBySignature(). Hibernate id Type: ''{0}''", hbIdType));
		}
		
		Serializable idValue = null;
		
		PlayerResultSet playerResultSet = new PlayerResultSet(rawKeyValues);
		try {
			 idValue = (Serializable) 
					this.runByReflection(
						hbIdType.getClass().getName(),
						"nullSafeGet",
						new String[] {
							ResultSet.class.getName(),
							String[].class.getName(),
							"org.hibernate.engine.spi.SharedSessionContractImplementor",
							Object.class.getName()
						},
						hbIdType,
						new Object[]{
							playerResultSet,
							playerResultSet.getColumnNames(),
							this.manager.getConfig().getSessionFactory().getCurrentSession(),
							null
						}
					);
			
//			idValue = (Serializable) hbIdType.nullSafeGet(playerResultSet, playerResultSet.getColumnNames(),
//					(org.hibernate.engine.SessionImplementor) this.manager.getConfig().getSessionFactory().getCurrentSession(), null);
		} catch (HibernateException e) {
			throw new RuntimeException("This should not happen. prpType: ");
		}
		
		return idValue;
	}
	
	@Override
	public Serializable getIdValue(Object entityInstanceOrProxy) {
		Class<?> entityClass = null;
		if (entityInstanceOrProxy instanceof HibernateProxy) {
			entityClass = (Class<?>) entityInstanceOrProxy.getClass().getSuperclass();
		} else {
			entityClass = (Class<?>) entityInstanceOrProxy.getClass();
		}
		ClassMetadata classMetadata = this.getAllClassMetadata().get(entityClass.getName());
		PlayerStatment playerStatment = new PlayerStatment();
		
		Serializable idValue = 
				(Serializable) this.runByReflection(
				classMetadata.getClass().getName(),
				"getIdentifier",
				new String[]{ Object.class.getName(), "org.hibernate.engine.spi.SharedSessionContractImplementor"},
				classMetadata,
				new Object[]{ entityInstanceOrProxy, this.manager.getConfig().getSessionFactory().getCurrentSession()});
		return idValue;
	}
	
	@Override
	protected Map<String, ClassMetadata> getAllClassMetadata() {
		Object sessionFactoryImplementor = this.manager.getConfig().getSessionFactory();
		Object metamodelImplementor = this.runByReflection(
			"org.hibernate.engine.spi.SessionFactoryImplementor",
			"getMetamodel",
			new String[]{},
			sessionFactoryImplementor,
			new Object[]{}
		);
		@SuppressWarnings("unchecked")
		Map<String, Object> entityPersisters = (Map<String, Object>) this.runByReflection(
			"org.hibernate.metamodel.spi.MetamodelImplementor",
			"entityPersisters",
			new String[]{},
			metamodelImplementor,
			new Object[]{}
		);
		//SessionFactoryImplementor sessionFactoryImplementor = ((SessionFactoryImplementor)this.manager.getConfig().getSessionFactory());
		//MetamodelImplementor metamodelImplementor = sessionFactoryImplementor.getMetamodel();
		//Map<String, EntityPersister> entityPersisters = metamodelImplementor.entityPersisters();
		Map<String, ClassMetadata> classMetadatas = new LinkedHashMap<String, ClassMetadata>();
		for (String ettName : entityPersisters.keySet()) {
			if (entityPersisters.get(ettName) instanceof ClassMetadata) {
				classMetadatas.put(ettName, (ClassMetadata) entityPersisters.get(ettName));				
			} else {
				throw new RuntimeException("This should not happen. entityPersister: " + entityPersisters.get(ettName));
			}
		}
		return classMetadatas;
	}
}
