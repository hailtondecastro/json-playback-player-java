package org.jsonplayback.hbsupport;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.PluralAttribute.CollectionType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.BagType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.ListType;
import org.hibernate.type.MapType;
import org.hibernate.type.SetType;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.hibernate.AssociationAndComponentPath;
import org.jsonplayback.player.hibernate.AssociationAndComponentPathKey;
import org.jsonplayback.player.hibernate.AssociationAndComponentTrackInfo;
import org.jsonplayback.player.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hb6Support implements HbObjPersistenceSupport {
	private static Logger logger = LoggerFactory.getLogger(Hb6Support.class);

	protected IPlayerManager manager;
	private Map<AssociationAndComponentPathKey, AssociationAndComponentPathHb6Support> associationAndCompositiesMap = new HashMap<>();
	protected Map<String, EntityType<?>> persistentClasses = new HashMap<>();
	private Set<Class<?>> compositiesSet = new HashSet<>();

	public Hb6Support(IPlayerManager manager) {
		this.manager = manager;
	}

	@Override
	public boolean isPersistentCollection(Object coll) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCollectionLazyUnitialized(Object coll, Object rootOwner, String pathFromOwner) {
	    PersistenceUnitUtil unitUtil = (PersistenceUnitUtil) ReflectionUtil.runByReflection(
    		"org.hibernate.SessionFactory",
    		"getPersistenceUnitUtil",
    		new String[]{},
    		this.manager.getConfig().getSessionFactory(),
	    	new Object[]{}
    	);
	    return unitUtil.isLoaded(rootOwner, pathFromOwner);
	}

	@Override
	public boolean isHibernateProxyLazyUnitialized(Object hProxy) {
	    PersistenceUnitUtil unitUtil = (PersistenceUnitUtil) ReflectionUtil.runByReflection(
	    		"org.hibernate.SessionFactory",
	    		"getPersistenceUnitUtil",
	    		new String[]{},
	    		this.manager.getConfig().getSessionFactory(),
		    	new Object[]{}
	    	);
	    return unitUtil.isLoaded(hProxy);
	}

	@Override
	public Connection getConnection() {
		final AtomicReference<Connection> connRef = new AtomicReference<>();
		this.manager.getConfig().getSessionFactory().getCurrentSession().doWork(connection -> {
			connRef.set(connection);
		});
		return connRef.get();
	}

//	@Override
//	public Object getCollectionOwner(Object coll) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public String getCollectionFieldName(Object coll) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public String getCollectionGetRole(Object coll) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getRawKeyValuesFromHbProxy(Object hibernateProxy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getRawKeyValuesFromNonHbProxy(Object nonHibernateProxy) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void collectAssociationAndCompositiesMap() {
		if (logger.isDebugEnabled()) {
			logger.debug("collectAssociationAndCompositiesMap()");
		}
		Metamodel metamodel = (Metamodel) ReflectionUtil.runByReflection(
			"org.hibernate.SessionFactory",
			"getMetamodel",
			new String[]{},
			this.manager.getConfig().getSessionFactory(),
			new Object[]{});
		Set<EntityType<?>> entityTypes = metamodel.getEntities();
		for (EntityType<?> entityTypeItem : entityTypes) {
			this.persistentClasses.put(entityTypeItem.getJavaType().getName(), entityTypeItem);
		}
		
		for (String entityName : this.persistentClasses.keySet()) {
			EntityType<?> classMetadata = this.persistentClasses.get(entityName);

			Class<?> ownerRootClass;
			try {
				ownerRootClass = classMetadata.getJavaType();
			} catch (Exception e) {
				throw new RuntimeException(classMetadata.getName() + " not supported.", e);
			}

			List<Type<?>> singAllPrpsAndIdTypes = new ArrayList<>();
			List<String> singAllPrpsAndIdNames = new ArrayList<>();
			for (Attribute<?, ?> prpAtt : classMetadata.getSingularAttributes()) {
				singAllPrpsAndIdTypes.add(classMetadata.getSingularAttribute(prpAtt.getName(), prpAtt.getJavaType()).getType());
				singAllPrpsAndIdNames.add(prpAtt.getName());
			}
			singAllPrpsAndIdTypes.add(classMetadata.getIdType());
			singAllPrpsAndIdNames.add(classMetadata.getId(classMetadata.getIdType().getJavaType()).getName());
			for (int i = 0; i < singAllPrpsAndIdTypes.size(); i++) {
				Type<?> prpType = singAllPrpsAndIdTypes.get(i);
				String prpName = singAllPrpsAndIdNames.get(i);
				AssociationAndComponentPathKey aacKeyFromRoot;
				if (prpType instanceof CompositeType) {
					Stack<String> pathStack = new Stack<>();
					Stack<EmbeddableType<?>> compositeTypePathStack = new Stack<>();
					pathStack.push(prpName);
					this.collectAssociationAndCompositiesMapRecursive(classMetadata, null, (EmbeddableType<?>) prpType,
							pathStack, compositeTypePathStack);
				} else if (prpType instanceof EntityType) {
					EntityType<?> entityType = (EntityType<?>) prpType;

					aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, prpName);

					AssociationAndComponentPathHb6Support relEacPath = new AssociationAndComponentPathHb6Support();
					relEacPath.setAacKey(aacKeyFromRoot);
					relEacPath.setCompositeTypePath(new EmbeddableType<?>[] {});
					relEacPath.setCompType(null);
					relEacPath.setRelEntity(entityType);
					relEacPath.setCollType(null);
					relEacPath.setCompositePrpPath(new String[] {});
					this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPath);
				}
			}
			
//			List<PluralAttribute<?, ?, ?>> plurAllPrpAtts = new ArrayList<>();
//			List<String> plurAllPrpNames = new ArrayList<>();
			for (PluralAttribute<?, ?, ?> prpAtt : classMetadata.getPluralAttributes()) {
				AssociationAndComponentPathKey aacKeyFromRoot;
				
//				plurAllPrpAtts.add(prpAtt);
				
				//CollectionType collType = (CollectionType) prpType;

				aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, prpAtt.getName());

				AssociationAndComponentPathHb6Support relEacPath = new AssociationAndComponentPathHb6Support();
				relEacPath.setAacKey(aacKeyFromRoot);
				relEacPath.setCompositeTypePath(new EmbeddableType<?>[]{});
				relEacPath.setCompType(null);
				relEacPath.setRelEntity(null);
				relEacPath.setCollType(prpAtt.getCollectionType());
				relEacPath.setCompositePrpPath(new String[] {});
				this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPath);
			}			
		}
		for (AssociationAndComponentPathKey key : this.associationAndCompositiesMap.keySet()) {
			AssociationAndComponentPathHb6Support aacPath = this.associationAndCompositiesMap.get(key);
			if (aacPath.getCompType() != null) {
				Class<?> compositeClass = this.associationAndCompositiesMap.get(key).getCompType().getJavaType();
				this.compositiesSet.add(compositeClass);
			}
		}
	}
	
	protected void collectAssociationAndCompositiesMapRecursive(EntityType<?> ownerRootClassMetadata,
			EmbeddableType<?> ownerCompositeType, EmbeddableType<?> compositeType, Stack<String> pathStack,
			Stack<EmbeddableType<?>> compositeTypePathStack) {
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("Collecting CompositeType:{0}", compositeType));
		}
		Class<?> ownerRootClass;
		try {
			ownerRootClass = ownerRootClassMetadata.getJavaType();
		} catch (Exception e) {
			throw new RuntimeException(ownerRootClassMetadata.getName() + " not supported.", e);
		}
		String pathFromStack = this.mountPathFromStack(pathStack);
		AssociationAndComponentPathKey aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass,
				pathFromStack);
		if (!this.associationAndCompositiesMap.containsKey(aacKeyFromRoot)) {
			AssociationAndComponentPathHb6Support aacPath = new AssociationAndComponentPathHb6Support();
			aacPath.setAacKey(aacKeyFromRoot);
			aacPath.setCompositeTypePath(new EmbeddableType<?>[] { compositeType });
			aacPath.setCompType(compositeType);
			aacPath.setRelEntity(null);
			aacPath.setCollType(null);
			aacPath.setCompositePrpPath(new String[] { pathStack.peek() });
			this.associationAndCompositiesMap.put(aacKeyFromRoot, aacPath);

			List<Type> singAllPrpsTypes = new ArrayList<>();
			List<String> singAllPrpsNames = new ArrayList<>();
			for (Attribute<?, ?> prpAtt : compositeType.getSingularAttributes()) {
				singAllPrpsTypes.add(compositeType.getSingularAttribute(prpAtt.getName()).getType());
				singAllPrpsNames.add(prpAtt.getName());
			}
			for (int i = 0; i < singAllPrpsTypes.size(); i++) {
				Type subPrpType = singAllPrpsTypes.get(i);
				String subPrpName = singAllPrpsNames.get(i);

				if (subPrpType.getPersistenceType() == PersistenceType.EMBEDDABLE) {
					if (logger.isTraceEnabled()) {
						logger.trace(MessageFormat.format("Recursion on collect CompositeType: {0} -> {1}",
								compositeType.getJavaType().getName(), subPrpName));
					}
					pathStack.push(subPrpName);
					compositeTypePathStack.push((EmbeddableType<?>) subPrpType);
					this.collectAssociationAndCompositiesMapRecursive(ownerRootClassMetadata, compositeType,
							(EmbeddableType<?>) subPrpType, pathStack, compositeTypePathStack);
				} else if (subPrpType.getPersistenceType() == PersistenceType.ENTITY) {
					EntityType<?> entityType = (EntityType<?>) subPrpType;
					Stack<String> pathStackRelation = new Stack<String>();
					pathStackRelation.addAll(pathStack);
					pathStackRelation.push(subPrpName);
					String pathStackRelationStr = this.mountPathFromStack(pathStackRelation);
			
					aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, pathStackRelationStr);

					if (!this.associationAndCompositiesMap.containsKey(aacKeyFromRoot)) {
						AssociationAndComponentPathHb6Support relEacPathFromRoot = new AssociationAndComponentPathHb6Support();
						relEacPathFromRoot.setAacKey(aacKeyFromRoot);
						relEacPathFromRoot.setCompositeTypePath(
								compositeTypePathStack.toArray(new EmbeddableType<?>[compositeTypePathStack.size()]));
						relEacPathFromRoot.setCompType(null);
						relEacPathFromRoot.setRelEntity(entityType);
						relEacPathFromRoot.setCollType(null);
						relEacPathFromRoot.setCompositePrpPath(pathStack.toArray(new String[pathStack.size()]));
						this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPathFromRoot);
					}
				}
//				else if (subPrpType instanceof CollectionType) {
//
//				}
			}
			
			for (PluralAttribute<?, ?, ?> prpAtt : compositeType.getPluralAttributes()) {
				
				Stack<String> pathStackRelation = new Stack<String>();
				pathStackRelation.addAll(pathStack);
				pathStackRelation.push(prpAtt.getName());
				String pathStackRelationStr = this.mountPathFromStack(pathStackRelation);
			
				aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, pathStackRelationStr);
				if (!this.associationAndCompositiesMap.containsKey(aacKeyFromRoot)) {
					AssociationAndComponentPathHb6Support relEacPathFromRoot = new AssociationAndComponentPathHb6Support();
					relEacPathFromRoot.setAacKey(aacKeyFromRoot);
					relEacPathFromRoot.setCompositeTypePath(
							compositeTypePathStack.toArray(new EmbeddableType<?>[compositeTypePathStack.size()]));
					relEacPathFromRoot.setCompType(null);
					relEacPathFromRoot.setRelEntity(null);
					relEacPathFromRoot.setCollType(prpAtt.getCollectionType());
					relEacPathFromRoot.setCompositePrpPath(pathStack.toArray(new String[pathStack.size()]));
					this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPathFromRoot);
				}
				
			}			
		} else {
			// maybe it is deprecated!?
			EmbeddableType<?> existingComponent = this.associationAndCompositiesMap.get(aacKeyFromRoot).getCompType();
			boolean isDifferent = false;
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"Component already collected, verifying if the definition is the same: {0}", compositeType));
			}
			if (existingComponent.getSingularAttributes().size() == compositeType.getSingularAttributes().size()) {
				List<SingularAttribute<?, ?>> existingComponentAttsArrL = new ArrayList<>(existingComponent.getSingularAttributes());
				List<SingularAttribute<?, ?>> componentAttsArrL = new ArrayList<>(compositeType.getSingularAttributes());
				for (int i = 0; i < compositeType.getSingularAttributes().size(); i++) {
					if (existingComponentAttsArrL.get(i).getJavaType() != componentAttsArrL.get(i).getJavaType()) {
						isDifferent = true;
						break;
					}
				}
			} else {
				if (logger.isTraceEnabled()) {
					logger.trace(MessageFormat.format(
							"Component already collected, the both has the same definition, ok: {0}, {1}",
							existingComponent, compositeType));
				}
				isDifferent = true;
			}
			if (isDifferent) {
				throw new RuntimeException(
						MessageFormat.format("CompositeType's diferentes: {0}, {1}", existingComponent, compositeType));
			}
		}
		pathStack.pop();
		if (ownerCompositeType != null) {
			compositeTypePathStack.pop();
		}
	}
	
	@Override
	public void init() {
		this.associationAndCompositiesMap.clear();
		this.collectAssociationAndCompositiesMap();
	}

	@Override
	public boolean isComponent(Class<?> componentClass) {
		return this.compositiesSet.contains(componentClass);
	}

	@Override
	public boolean isPersistentClass(Class<?> clazz) {
		if (this.persistentClasses.containsKey(clazz.getName())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isCollectionRelationship(Class<?> ownerClass, String pathFromOwner) {
		AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(ownerClass, pathFromOwner);
		if (this.associationAndCompositiesMap.containsKey(aacKey)) {
			AssociationAndComponentPathHb6Support aacPath = this.associationAndCompositiesMap.get(aacKey);
			return aacPath.getCollType() != null;
		} else {
			return false;
		}
	}

	@Override
	public boolean isManyToOneRelationship(Class<?> ownerClass, String pathFromOwner) {
		AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(ownerClass, pathFromOwner);
		AssociationAndComponentPathHb6Support entityAndComponentPath = this.associationAndCompositiesMap.get(aacKey);
		if (entityAndComponentPath != null) {
			return entityAndComponentPath.getRelEntity() != null;
		} else {
			return false;			
		}
	}

	@Override
	public boolean isComponentOrRelationship(Class<?> ownerClass, String pathFromOwner) {
		AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(ownerClass, pathFromOwner);
		return this.associationAndCompositiesMap.containsKey(aacKey);
	}

	@Override
	public boolean isComponentByTrack(AssociationAndComponentTrackInfo aacTrackInfo) {
		AssociationAndComponentPathHb6Support aacOnPath = this.associationAndCompositiesMap.get(aacTrackInfo);
		if (aacTrackInfo.getEntityAndComponentPath() instanceof AssociationAndComponentPathHbSupport) {
			return ((AssociationAndComponentPathHbSupport)aacTrackInfo.getEntityAndComponentPath()).getCollType() != null;
		} else {
			throw new RuntimeException("This should not happen. prpType: ");
		}
	}

	@Override
	public Serializable getIdValue(Class<?> entityClass, Object[] rawKeyValues) {
		return null;
	}

	@Override
	public Serializable getIdValue(Object entityInstanceOrProxy) {
	    PersistenceUnitUtil unitUtil = (PersistenceUnitUtil) ReflectionUtil.runByReflection(
	    		"org.hibernate.SessionFactory",
	    		"getPersistenceUnitUtil",
	    		new String[]{},
	    		this.manager.getConfig().getSessionFactory(),
		    	new Object[]{}
	    	);
	    return (Serializable) unitUtil.getIdentifier(entityInstanceOrProxy);
	}

	@SuppressWarnings({ "unused", "deprecation" })
	@Override
	public Object getById(Class<?> entityClass, Object idValue) {
		PersistenceUnitUtil unitUtil = (PersistenceUnitUtil) ReflectionUtil.runByReflection(
	    		"org.hibernate.SessionFactory",
	    		"getPersistenceUnitUtil",
	    		new String[]{},
	    		this.manager.getConfig().getSessionFactory(),
		    	new Object[]{}
	    	);
		return this.manager.getConfig().getSessionFactory().getCurrentSession().get(entityClass, (Serializable)idValue);
	}

	@Override
	public AssociationAndComponentPath getAssociationAndComponentOnPath(Class<?> ownerClass, String pathStr) {
		AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(ownerClass, pathStr);
		return this.associationAndCompositiesMap.get(aacKey);
	}

	@Override
	public boolean testCollectionStyle(Class<?> ownerClass, String prpName, CollectionStyle style) {
		EntityType<?> classMetadata = this.persistentClasses.get(ownerClass.getName());
		if (classMetadata != null) {
			Attribute<?, ?> attr = classMetadata.getAttribute(prpName);
			if (attr instanceof PluralAttribute) {
				PluralAttribute<?, ?, ?> plrAttr = (PluralAttribute<?, ?, ?>) attr;
				if (style == CollectionStyle.SET && plrAttr.getCollectionType() == CollectionType.SET) {
					return true;
				} else if (style == CollectionStyle.BAG && plrAttr.getCollectionType() == CollectionType.COLLECTION) {
					return true;
				} else if (style == CollectionStyle.LIST && plrAttr.getCollectionType() == CollectionType.LIST) {
					return true;
				} else if (style == CollectionStyle.MAP && plrAttr.getCollectionType() == CollectionType.MAP) {
					return true;
				}
			} else { 
				return false;
			}
		}
		return false;
	}

	@Override
	public <R> CriteriaCompat<R> createCriteria(EntityManager em, Class<R> clazz) {
		return new CriteriaCompatBase<>(em,  clazz);
	}
	
	@Override
	public <R> CriteriaCompat<R> createCriteria(Session session, Class<R> clazz) {
		return new CriteriaCompatBase<>(session,  clazz);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void processNewInstantiate(Class<?> instType, Object instValue) {
		EntityType<?> classMetadata = this.persistentClasses.get(instType.getName());
		if (classMetadata != null) {
			@SuppressWarnings("unused")
			PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(instType);
			for (PluralAttribute<?, ?, ?> pluralAttribute : classMetadata.getPluralAttributes()) {
				Collection resultColl = null;
				if (pluralAttribute.getCollectionType() == CollectionType.SET) {
					resultColl = new LinkedHashSet<>();
				} else if (pluralAttribute.getCollectionType() == CollectionType.LIST) {
					throw new RuntimeException("Not supported. prpType: " + pluralAttribute.getCollectionType());
				} else if (pluralAttribute.getCollectionType() == CollectionType.COLLECTION) {
					throw new RuntimeException("Not supported. prpType: " + pluralAttribute.getCollectionType());
				} else {
					throw new RuntimeException("This should not happen. prpType: " + pluralAttribute.getCollectionType());
				}
				try {
					PropertyUtils.setProperty(instValue, pluralAttribute.getName(), resultColl);
				} catch (Exception e) {
					throw new RuntimeException("This should not happen. prpType: " + pluralAttribute.getCollectionType(), e);
				}
			}
		}				
	}

	@Override
	public String getPlayerObjectIdPrpName(Class clazz) {
		EntityType<?> entityType = this.persistentClasses.get(clazz.getName());
		return entityType.getId(entityType.getIdType().getJavaType()).getName();
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
}
