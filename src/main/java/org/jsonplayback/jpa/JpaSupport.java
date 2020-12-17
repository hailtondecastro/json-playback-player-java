package org.jsonplayback.jpa;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.jsonplayback.hibernate.CollectionStyle;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.ObjPersistenceSupport;
import org.jsonplayback.player.implementation.AssociationAndComponentPath;
import org.jsonplayback.player.implementation.AssociationAndComponentPathKey;
import org.jsonplayback.player.implementation.AssociationAndComponentTrackInfo;
import org.jsonplayback.player.implementation.IPlayerManagerImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public abstract class JpaSupport implements ObjPersistenceSupport {
	private static Logger logger = LoggerFactory.getLogger(JpaSupport.class);

	private Map<AssociationAndComponentPathKey, AssociationAndComponentPathJpaSupport> associationAndCompositiesMap = new HashMap<>();
	protected Map<String, EntityType<?>> persistentClasses = new HashMap<>();
	private Set<Class<?>> compositiesSet = new HashSet<>();

	public abstract EntityManager getCurrentEntityManager();

	public JpaSupport() {
	}

//	@Override
//	public boolean isPersistentCollection(AssociationAndComponentTrackInfo aacTrackInfo, Object coll) {
//		if (aacTrackInfo != null) {
//			PersistenceUnitUtil unitUtil = this.getCurrentEntityManager().getEntityManagerFactory()
//					.getPersistenceUnitUtil();			
//		}
//		return false;
//	}

	Pattern fieldOnEmbNameRx = Pattern.compile("(.*)\\.([^\\.]+)");
	
	@Override
	public boolean isLazyUnitialized(Object coll, Object rootOwner, String pathFromOwner) {
		PersistenceUnitUtil unitUtil = this.getCurrentEntityManager().getEntityManagerFactory()
				.getPersistenceUnitUtil();
		Matcher matcher = fieldOnEmbNameRx.matcher(pathFromOwner);
		if (matcher.find()) {
			try {
				Object embOwner = PropertyUtils.getNestedProperty(rootOwner, matcher.group(1));
				return !unitUtil.isLoaded(embOwner, matcher.group(2));
			} catch (IllegalAccessException e) {
				throw new RuntimeException("This should not happen", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("This should not happen", e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("This should not happen", e);
			}
		} else {
			return !unitUtil.isLoaded(rootOwner, pathFromOwner);			
		}
	}

	@Override
	public boolean isLazyUnitialized(Object entity) {
		PersistenceUnitUtil unitUtil = this.getCurrentEntityManager().getEntityManagerFactory()
				.getPersistenceUnitUtil();
		return !unitUtil.isLoaded(entity);
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

//	@Override
//	public Object[] getRawKeyValuesFromHbProxy(Object hibernateProxy) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Object[] getRawKeyValuesFromNonHbProxy(Object nonHibernateProxy) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public void collectAssociationAndCompositiesMap() {
		if (logger.isDebugEnabled()) {
			logger.debug("collectAssociationAndCompositiesMap()");
		}
		Metamodel metamodel = this.getCurrentEntityManager().getMetamodel();
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
				singAllPrpsAndIdTypes
						.add(classMetadata.getSingularAttribute(prpAtt.getName(), prpAtt.getJavaType()).getType());
				singAllPrpsAndIdNames.add(prpAtt.getName());
			}
			singAllPrpsAndIdTypes.add(classMetadata.getIdType());
			singAllPrpsAndIdNames.add(classMetadata.getId(classMetadata.getIdType().getJavaType()).getName());
			for (int i = 0; i < singAllPrpsAndIdTypes.size(); i++) {
				Type<?> prpType = singAllPrpsAndIdTypes.get(i);
				String prpName = singAllPrpsAndIdNames.get(i);
				AssociationAndComponentPathKey aacKeyFromRoot;
				if (prpType instanceof EmbeddableType) {
					Stack<String> pathStack = new Stack<>();
					Stack<EmbeddableType<?>> compositeTypePathStack = new Stack<>();
					pathStack.push(prpName);
					this.collectAssociationAndCompositiesMapRecursive(classMetadata, null, (EmbeddableType<?>) prpType,
							pathStack, compositeTypePathStack);
				} else if (prpType instanceof EntityType) {
					EntityType<?> entityType = (EntityType<?>) prpType;

					aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, prpName);

					AssociationAndComponentPathJpaSupport relEacPath = new AssociationAndComponentPathJpaSupport();
					relEacPath.setAacKey(aacKeyFromRoot);
					relEacPath.setCompositeTypePath(new EmbeddableType<?>[] {});
					relEacPath.setEmbeddableType(null);
					relEacPath.setRelEntity(entityType);
					relEacPath.setPluralAttribuite(null);
					relEacPath.setCompositePrpPath(new String[] {});
					this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPath);
				}
			}

//			List<PluralAttribute<?, ?, ?>> plurAllPrpAtts = new ArrayList<>();
//			List<String> plurAllPrpNames = new ArrayList<>();
			for (PluralAttribute<?, ?, ?> prpAtt : classMetadata.getPluralAttributes()) {
				AssociationAndComponentPathKey aacKeyFromRoot;

//				plurAllPrpAtts.add(prpAtt);

				// CollectionType collType = (CollectionType) prpType;

				aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, prpAtt.getName());

				AssociationAndComponentPathJpaSupport relEacPath = new AssociationAndComponentPathJpaSupport();
				relEacPath.setAacKey(aacKeyFromRoot);
				relEacPath.setCompositeTypePath(new EmbeddableType<?>[] {});
				relEacPath.setEmbeddableType(null);
				relEacPath.setRelEntity(null);
				relEacPath.setPluralAttribuite(prpAtt);
				relEacPath.setCompositePrpPath(new String[] {});
				this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPath);
			}
		}
		for (AssociationAndComponentPathKey key : this.associationAndCompositiesMap.keySet()) {
			AssociationAndComponentPathJpaSupport aacPath = this.associationAndCompositiesMap.get(key);
			if (aacPath.getEmbeddableType() != null) {
				Class<?> compositeClass = this.associationAndCompositiesMap.get(key).getEmbeddableType().getJavaType();
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
			AssociationAndComponentPathJpaSupport aacPath = new AssociationAndComponentPathJpaSupport();
			aacPath.setAacKey(aacKeyFromRoot);
			aacPath.setCompositeTypePath(new EmbeddableType<?>[] { compositeType });
			aacPath.setEmbeddableType(compositeType);
			aacPath.setRelEntity(null);
			aacPath.setPluralAttribuite(null);
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
						AssociationAndComponentPathJpaSupport relEacPathFromRoot = new AssociationAndComponentPathJpaSupport();
						relEacPathFromRoot.setAacKey(aacKeyFromRoot);
						relEacPathFromRoot.setCompositeTypePath(
								compositeTypePathStack.toArray(new EmbeddableType<?>[compositeTypePathStack.size()]));
						relEacPathFromRoot.setEmbeddableType(null);
						relEacPathFromRoot.setRelEntity(entityType);
						relEacPathFromRoot.setPluralAttribuite(null);
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
					AssociationAndComponentPathJpaSupport relEacPathFromRoot = new AssociationAndComponentPathJpaSupport();
					relEacPathFromRoot.setAacKey(aacKeyFromRoot);
					relEacPathFromRoot.setCompositeTypePath(
							compositeTypePathStack.toArray(new EmbeddableType<?>[compositeTypePathStack.size()]));
					relEacPathFromRoot.setEmbeddableType(null);
					relEacPathFromRoot.setRelEntity(null);
					relEacPathFromRoot.setPluralAttribuite(prpAtt);
					relEacPathFromRoot.setCompositePrpPath(pathStack.toArray(new String[pathStack.size()]));
					this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPathFromRoot);
				}

			}
		} else {
			// maybe it is deprecated!?
			EmbeddableType<?> existingComponent = this.associationAndCompositiesMap.get(aacKeyFromRoot).getEmbeddableType();
			boolean isDifferent = false;
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"Component already collected, verifying if the definition is the same: {0}", compositeType));
			}
			if (existingComponent.getSingularAttributes().size() == compositeType.getSingularAttributes().size()) {
				List<SingularAttribute<?, ?>> existingComponentAttsArrL = new ArrayList<>(
						existingComponent.getSingularAttributes());
				List<SingularAttribute<?, ?>> componentAttsArrL = new ArrayList<>(
						compositeType.getSingularAttributes());
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

	protected ObjectMapper mapperForObjId;

	@Override
	public void init(IPlayerManagerImplementor playerManagerImplementor) {
		this.associationAndCompositiesMap.clear();
		this.collectAssociationAndCompositiesMap();

		JpaObjectIdBeanSerializerModifier modifier = new JpaObjectIdBeanSerializerModifier().configManager(playerManagerImplementor);
		// jsonComponentModule.addSerializer(PlayerSnapshot.class,
		// playerSnapshotSerializer);
		// mapper.registerModule(jsonComponentModule);
		this.mapperForObjId = new ObjectMapper();
		this.mapperForObjId.setConfig(this.mapperForObjId.getSerializationConfig().with(playerManagerImplementor.getConfig().getBasicClassIntrospector()));
		this.mapperForObjId.registerModule(new SimpleModule() {
			@Override
			public void setupModule(SetupContext context) {
				super.setupModule(context);
				context.addBeanSerializerModifier(modifier);
			}
		});
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
		if (pathFromOwner != null) {
			AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(ownerClass, pathFromOwner);
			if (this.associationAndCompositiesMap.containsKey(aacKey)) {
				AssociationAndComponentPathJpaSupport aacPath = this.associationAndCompositiesMap.get(aacKey);
				return aacPath.getPluralAttribuite() != null;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean isManyToOneRelationship(Class<?> ownerClass, String pathFromOwner) {
		if (pathFromOwner != null) {
			AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(ownerClass, pathFromOwner);
			AssociationAndComponentPathJpaSupport entityAndComponentPath = this.associationAndCompositiesMap.get(aacKey);
			if (entityAndComponentPath != null) {
				return entityAndComponentPath.getRelEntity() != null;
			} else {
				return false;
			}
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
		AssociationAndComponentPathJpaSupport aacOnPath = this.associationAndCompositiesMap.get(aacTrackInfo);
		if (aacTrackInfo.getEntityAndComponentPath() instanceof AssociationAndComponentPathJpaSupport) {
			return ((AssociationAndComponentPathJpaSupport) aacTrackInfo.getEntityAndComponentPath())
					.getPluralAttribuite() != null;
		} else {
			throw new RuntimeException("This should not happen. prpType: ");
		}
	}

//	@Override
//	public Serializable getIdValue(Class<?> entityClass, Object[] rawKeyValues) {
//		return null;
//	}

	@Override
	public Serializable getIdValue(Object entityInstanceOrProxy) {
		PersistenceUnitUtil unitUtil = this.getCurrentEntityManager().getEntityManagerFactory()
				.getPersistenceUnitUtil();
		return (Serializable) unitUtil.getIdentifier(entityInstanceOrProxy);
	}

	@SuppressWarnings({ "unused", "deprecation" })
	@Override
	public Object getById(Class<?> entityClass, Object idValue) {
		PersistenceUnitUtil unitUtil = this.getCurrentEntityManager().getEntityManagerFactory()
				.getPersistenceUnitUtil();
		this.deepGetReferenceForEmbeddedId(entityClass, idValue);
		return this.getCurrentEntityManager().find(entityClass, idValue);
	}
	
	/**
	 * Without this you will see: "org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing..." on 
	 * PlayerManagerTest.detailAKey0c0GetBySignTest() using Jpa mode.
	 * @param entityClass
	 * @param idValue
	 */
	private void deepGetReferenceForEmbeddedId(Class<?> entityClass, Object idValue) {
		PersistenceUnitUtil unitUtil = this.getCurrentEntityManager().getEntityManagerFactory()
				.getPersistenceUnitUtil();
		PropertyDescriptor[] prpDescsArr = PropertyUtils.getPropertyDescriptors(idValue.getClass());
		try {						
			for (PropertyDescriptor prpDescItem : prpDescsArr) {
				Object idPart = PropertyUtils.getProperty(idValue, prpDescItem.getName());
				if(idPart != null) {
					Class<?> idPartClass = this.unwrappRealType(idPart);
					if(this.isPersistentClass(idPartClass)) {
						Object idOfIdPart = unitUtil.getIdentifier(idPart);
						this.deepGetReferenceForEmbeddedId(idOfIdPart.getClass(), idOfIdPart);
						Object idPartAsRef = this.getCurrentEntityManager().getReference(idPart.getClass(), idOfIdPart);
						PropertyUtils.setProperty(idValue, prpDescItem.getName(), idPartAsRef);
					} else {
						// nothing							
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException("This should not happen", e);
		}
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
					throw new RuntimeException(
							"This should not happen. prpType: " + pluralAttribute.getCollectionType());
				}
				try {
					PropertyUtils.setProperty(instValue, pluralAttribute.getName(), resultColl);
				} catch (Exception e) {
					throw new RuntimeException(
							"This should not happen. prpType: " + pluralAttribute.getCollectionType(), e);
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

	@Override
	public void persistenceRemove(Object entity) {
		this.getCurrentEntityManager().remove(entity);
	}

	@Override
	public void persistencePersist(Object entity) {
		this.getCurrentEntityManager().persist(entity);
	}

	@Override
	public Object parseObjectId(IPlayerManager manager, Class<?> ownerClass, String stringifiedObjectId) {
		Object owner = null;
		try {
			owner = this.mapperForObjId.readValue(stringifiedObjectId, ownerClass);
		} catch (JsonParseException e) {
			throw new RuntimeException("This should not happen. stringifiedObjectId: " + stringifiedObjectId, e);
		} catch (JsonMappingException e) {
			throw new RuntimeException("This should not happen. stringifiedObjectId: " + stringifiedObjectId, e);
		} catch (IOException e) {
			throw new RuntimeException("This should not happen. stringifiedObjectId: " + stringifiedObjectId, e);
		}
		return this.getIdValue(owner);
	}

	@Override
	public String stringfyObjectId(IPlayerManager manager, Object owner) {
		try {
			return this.mapperForObjId.writeValueAsString(owner);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("This should not happen.", e);
		}
	}
	
	@Override
	public Class<?> unwrappRealType(Object possibleWrapperValue) {
		return this.unwrappRealType(possibleWrapperValue.getClass());
	}
	
	@Override
	public Class unwrappRealType(Class<?> possibleWrapperType) {
		if (this.isPersistentClass(possibleWrapperType)) {
			return possibleWrapperType;
		} else if (possibleWrapperType.getSuperclass() != null
				&& this.isPersistentClass(possibleWrapperType.getSuperclass())) {
			return possibleWrapperType.getSuperclass();
		} else {
			return possibleWrapperType;
		}
	}
	
	@Override
	public Set<Class<?>> allManagedTypes() {
		Set<Class<?>> result = new LinkedHashSet<>();
		for (String typeStr : this.persistentClasses.keySet()) {
			result.add(this.persistentClasses.get(typeStr).getJavaType());
		}
		result.addAll(this.compositiesSet);
		return result;
	}
}
