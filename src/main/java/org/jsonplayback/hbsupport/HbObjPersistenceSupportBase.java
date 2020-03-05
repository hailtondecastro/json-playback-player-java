package org.jsonplayback.hbsupport;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.BagType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.ListType;
import org.hibernate.type.MapType;
import org.hibernate.type.SetType;
//import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.ObjPersistenceSupport;
import org.jsonplayback.player.implementation.AssociationAndComponentPath;
import org.jsonplayback.player.implementation.AssociationAndComponentPathKey;
import org.jsonplayback.player.implementation.AssociationAndComponentTrackInfo;
import org.jsonplayback.player.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class HbObjPersistenceSupportBase implements ObjPersistenceSupport {
	private static Logger logger = LoggerFactory.getLogger(HbObjPersistenceSupportBase.class);

	//protected IPlayerManager manager;
	private Map<AssociationAndComponentPathKey, AssociationAndComponentPathObjPersistenceSupport> associationAndCompositiesMap = new HashMap<>();
	protected Map<String, ClassMetadata> persistentClasses = new HashMap<>();
	private Set<Class<?>> compositiesSet = new HashSet<>();
	
	public abstract SessionFactory getSessionFactory();
	public Session getCurrentSession() {
		return this.getSessionFactory().getCurrentSession();
	}

	public HbObjPersistenceSupportBase() {
		//this.manager = manager;
		this.persistentCollecitonClass = this.resolvePersistentCollectionClass();
		
		this.primitiveTypes = new HashMap<>();
		this.primitiveTypes.put(int.class.getName(), int.class);
		this.primitiveTypes.put(boolean.class.getName(), boolean.class);
		this.primitiveTypes.put(long.class.getName(), long.class);
	}
	private Map<String, Class<?>> primitiveTypes;
	
	protected Class<?> persistentCollecitonClass;

	@Override
	public boolean isPersistentCollection(Object coll) {
		return persistentCollecitonClass.isAssignableFrom(coll.getClass());
	}

	@Override
	public abstract boolean isCollectionLazyUnitialized(Object coll, Object owner, String pathFromOwner);
	
	public abstract Class<?> resolvePersistentCollectionClass();
	
	public Class<?> getPersistentCollecitonClass() {
		return persistentCollecitonClass;
	}

//	@Override
//	public abstract Object getCollectionOwner(Object coll);

//	@Override
//	public String getCollectionFieldName(Object coll) {
//		Pattern rxCollectionRole = Pattern
//				.compile("^" + Pattern.quote(this.getCollectionOwner(coll).getClass().getName()) + "\\.(.*)");
//
//		String role = this.getCollectionGetRole(coll);
//		Matcher matcher = rxCollectionRole.matcher(role);
//		if (!matcher.find()) {
//			throw new RuntimeException(
//					MessageFormat.format("Collection role doesn't matches the expected pattern: ''{0}''", role));
//		}
//
//		// Class<?> ownerClass = this.getCollectionOwner(coll).getClass();
//		String fieldName = matcher.group(1);
//		return fieldName;
//	}

	//@Override
	public abstract  Object[] getRawKeyValuesFromHbProxy(Object hibernateProxy);

	//@Override
	public abstract Object[] getRawKeyValuesFromNonHbProxy(Object nonHibernateProxy);

	@Override
	public abstract String getCollectionGetRole(Object coll);

	@Override
	public boolean isHibernateProxyLazyUnitialized(Object hProxy) {
		return ((HibernateProxy) hProxy).getHibernateLazyInitializer().isUninitialized();
	}

//	@Override
//	public Connection getConnection() {
//		final AtomicReference<Connection> connRef = new AtomicReference<>();
//		this.getSessionFactory().getCurrentSession().doWork(connection -> {
//			connRef.set(connection);
//		});
//		return connRef.get();
//	}
	
	protected String mountPathFromStack(Collection<String> pathStack) {
		String pathResult = "";
		String dotStr = "";
		for (String pathItem : pathStack) {
			pathResult += dotStr + pathItem;
			dotStr = ".";
		}
		return pathResult;
	}

	protected void collectAssociationAndCompositiesMapRecursive(ClassMetadata ownerRootClassMetadata,
			CompositeType ownerCompositeType, CompositeType compositeType, Stack<String> pathStack,
			Stack<CompositeType> compositeTypePathStack) {
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("Collecting CompositeType:{0}", compositeType));
		}
		Class<?> ownerRootClass;
		try {
			ownerRootClass = Class.forName(ownerRootClassMetadata.getEntityName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(ownerRootClassMetadata.getEntityName() + " not supported.", e);
		}
		String pathFromStack = this.mountPathFromStack(pathStack);
		AssociationAndComponentPathKey aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass,
				pathFromStack);
		if (!this.associationAndCompositiesMap.containsKey(aacKeyFromRoot)) {
			AssociationAndComponentPathObjPersistenceSupport aacPath = new AssociationAndComponentPathObjPersistenceSupport();
			aacPath.setAacKey(aacKeyFromRoot);
			aacPath.setCompositeTypePath(new CompositeType[] { compositeType });
			aacPath.setCompType(compositeType);
			aacPath.setRelEntity(null);
			aacPath.setCollType(null);
			aacPath.setCompositePrpPath(new String[] { pathStack.peek() });
			this.associationAndCompositiesMap.put(aacKeyFromRoot, aacPath);

			List<Type> allPrps = new ArrayList<>();
			List<String> allPrpsNames = new ArrayList<>();
			allPrps.addAll(Arrays.asList(compositeType.getSubtypes()));
			allPrpsNames.addAll(Arrays.asList(compositeType.getPropertyNames()));
			for (int i = 0; i < allPrps.size(); i++) {
				Type subPrpType = allPrps.get(i);
				String subPrpName = allPrpsNames.get(i);

				if (subPrpType instanceof CompositeType) {
					if (logger.isTraceEnabled()) {
						logger.trace(MessageFormat.format("Recursion on collect CompositeType: {0} -> {1}",
								compositeType.getReturnedClass().getName(), subPrpName));
					}
					pathStack.push(subPrpName);
					compositeTypePathStack.push((CompositeType) subPrpType);
					this.collectAssociationAndCompositiesMapRecursive(ownerRootClassMetadata, compositeType,
							(CompositeType) subPrpType, pathStack, compositeTypePathStack);
				} else if (subPrpType instanceof EntityType) {
					EntityType entityType = (EntityType) subPrpType;
					Stack<String> pathStackRelation = new Stack<String>();
					pathStackRelation.addAll(pathStack);
					pathStackRelation.push(subPrpName);
					String pathStackRelationStr = this.mountPathFromStack(pathStackRelation);
			
					aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, pathStackRelationStr);

					if (!this.associationAndCompositiesMap.containsKey(aacKeyFromRoot)) {
						AssociationAndComponentPathObjPersistenceSupport relEacPathFromRoot = new AssociationAndComponentPathObjPersistenceSupport();
						relEacPathFromRoot.setAacKey(aacKeyFromRoot);
						relEacPathFromRoot.setCompositeTypePath(
								compositeTypePathStack.toArray(new CompositeType[compositeTypePathStack.size()]));
						relEacPathFromRoot.setCompType(null);
						relEacPathFromRoot.setRelEntity(entityType);
						relEacPathFromRoot.setCollType(null);
						relEacPathFromRoot.setCompositePrpPath(pathStack.toArray(new String[pathStack.size()]));
						this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPathFromRoot);
					}
				} else if (subPrpType instanceof CollectionType) {
					CollectionType collType = (CollectionType) subPrpType;
					Stack<String> pathStackRelation = new Stack<String>();
					pathStackRelation.addAll(pathStack);
					pathStackRelation.push(subPrpName);
					String pathStackRelationStr = this.mountPathFromStack(pathStackRelation);
				
					aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, pathStackRelationStr);
					if (!this.associationAndCompositiesMap.containsKey(aacKeyFromRoot)) {
						AssociationAndComponentPathObjPersistenceSupport relEacPathFromRoot = new AssociationAndComponentPathObjPersistenceSupport();
						relEacPathFromRoot.setAacKey(aacKeyFromRoot);
						relEacPathFromRoot.setCompositeTypePath(
								compositeTypePathStack.toArray(new CompositeType[compositeTypePathStack.size()]));
						relEacPathFromRoot.setCompType(null);
						relEacPathFromRoot.setRelEntity(null);
						relEacPathFromRoot.setCollType(collType);
						relEacPathFromRoot.setCompositePrpPath(pathStack.toArray(new String[pathStack.size()]));
						this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPathFromRoot);
					}
				}
			}
		} else {
			// maybe it is deprecated!?
			CompositeType existingComponent = this.associationAndCompositiesMap.get(aacKeyFromRoot).getCompType();
			boolean isDifferent = false;
			if (logger.isTraceEnabled()) {
				logger.trace(MessageFormat.format(
						"Component already collected, verifying if the definition is the same: {0}", compositeType));
			}
			if (existingComponent.getSubtypes().length == compositeType.getSubtypes().length) {
				for (int i = 0; i < compositeType.getSubtypes().length; i++) {
					if (existingComponent.getSubtypes()[i].getReturnedClass() != compositeType.getSubtypes()[i]
							.getReturnedClass()) {
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
	
	@SuppressWarnings("unchecked")
	protected Map<String,ClassMetadata> getAllClassMetadata() {
		return (Map<String, ClassMetadata>) this.runByReflection(
				"org.hibernate.SessionFactory",
				"getAllClassMetadata",
				new String[]{},
				this.getSessionFactory(),
				new Object[]{});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void collectAssociationAndCompositiesMap() {
		if (logger.isDebugEnabled()) {
			logger.debug("collectAssociationAndCompositiesMap()");
		}
		this.persistentClasses = this.getAllClassMetadata();
		
		for (String entityName : this.persistentClasses.keySet()) {
			ClassMetadata classMetadata = this.persistentClasses.get(entityName);

			Class<?> ownerRootClass;
			try {
				ownerRootClass = Class.forName(classMetadata.getEntityName());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(classMetadata.getEntityName() + " not supported.", e);
			}

			List<Type> allPrpsAndId = new ArrayList<>();
			List<String> allPrpsAndIdNames = new ArrayList<>();
			allPrpsAndId.addAll(Arrays.asList(classMetadata.getPropertyTypes()));
			allPrpsAndId.add(classMetadata.getIdentifierType());
			allPrpsAndIdNames.addAll(Arrays.asList(classMetadata.getPropertyNames()));
			allPrpsAndIdNames.add(classMetadata.getIdentifierPropertyName());
			for (int i = 0; i < allPrpsAndId.size(); i++) {
				Type prpType = allPrpsAndId.get(i);
				String prpName = allPrpsAndIdNames.get(i);
				AssociationAndComponentPathKey aacKeyFromRoot;
				if (prpType instanceof CompositeType) {
					Stack<String> pathStack = new Stack<>();
					Stack<CompositeType> compositeTypePathStack = new Stack<>();
					pathStack.push(prpName);
					this.collectAssociationAndCompositiesMapRecursive(classMetadata, null, (CompositeType) prpType,
							pathStack, compositeTypePathStack);
				} else if (prpType instanceof EntityType) {
					EntityType entityType = (EntityType) prpType;

					aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, prpName);

					AssociationAndComponentPathObjPersistenceSupport relEacPath = new AssociationAndComponentPathObjPersistenceSupport();
					relEacPath.setAacKey(aacKeyFromRoot);
					relEacPath.setCompositeTypePath(new CompositeType[] {});
					relEacPath.setCompType(null);
					relEacPath.setRelEntity(entityType);
					relEacPath.setCollType(null);
					relEacPath.setCompositePrpPath(new String[] {});
					this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPath);
				} else if (prpType instanceof CollectionType) {
					CollectionType collType = (CollectionType) prpType;

					aacKeyFromRoot = new AssociationAndComponentPathKey(ownerRootClass, prpName);

					AssociationAndComponentPathObjPersistenceSupport relEacPath = new AssociationAndComponentPathObjPersistenceSupport();
					relEacPath.setAacKey(aacKeyFromRoot);
					relEacPath.setCompositeTypePath(new CompositeType[] {});
					relEacPath.setCompType(null);
					relEacPath.setRelEntity(null);
					relEacPath.setCollType(collType);
					relEacPath.setCompositePrpPath(new String[] {});
					this.associationAndCompositiesMap.put(aacKeyFromRoot, relEacPath);
				}
			}
		}
		for (AssociationAndComponentPathKey key : this.associationAndCompositiesMap.keySet()) {
			AssociationAndComponentPathObjPersistenceSupport aacPath = this.associationAndCompositiesMap.get(key);
			if (aacPath.getCompType() != null) {
				Class<?> compositeClass = this.associationAndCompositiesMap.get(key).getCompType().getReturnedClass();
				this.compositiesSet.add(compositeClass);
			}
		}
	}
	
	@Override
	public boolean isComponent(Class<?> componentClass) {
		return this.compositiesSet.contains(componentClass);
	}

	@Override
	public void init() {
		this.associationAndCompositiesMap.clear();
		this.collectAssociationAndCompositiesMap();
	}
//
//	@Override
//	public boolean isRelationship(Class<?> clazz, String fieldName) {
//		if (clazz == null) {
//			throw new IllegalArgumentException("clazz can not be null");
//		}
//		if (fieldName == null || fieldName.trim().isEmpty()) {
//			throw new IllegalArgumentException("fieldName can not be null");
//		}
//
//		ClassMetadata classMetadata = this.persistentClasses.get(clazz.getName());
//		CompositeType compositeType = null;
//		if (classMetadata == null) {
//			AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(clazz, fieldName);
//			compositeType = this.associationAndCompositiesMap.get(aacKey).getCompType();
//			if (compositeType == null) {
//				throw new RuntimeException("Class is not mapped and is not a know CompositeType: " + clazz);
//			}
//		}
//		Type prpType = null;
//		if (classMetadata != null) {
//			boolean hasProperty = false;
//			if (fieldName.contains(".")) {
//				hasProperty = true;
//			} else {
//				String[] prpNames = classMetadata.getPropertyNames();
//				for (int i = 0; i < prpNames.length; i++) {
//					String prpNameItem = prpNames[i];
//					if (prpNameItem.equals(fieldName)) {
//						hasProperty = true;
//						break;
//					}
//				}
//			}
//
//			if (hasProperty) {
//				try {
//					prpType = classMetadata.getPropertyType(fieldName);
//				} catch (HibernateException he) {
//					throw new RuntimeException(MessageFormat.format("This should not happen for property: {0}.{1}",
//							classMetadata.getEntityName(), fieldName), he);
//				}
//			}		
//		} else {
//			String[] prpNames = compositeType.getPropertyNames();
//			for (int i = 0; i < prpNames.length; i++) {
//				String prpNameItem = prpNames[i];
//				if (prpNameItem.equals(fieldName)) {
//					prpType = compositeType.getSubtypes()[i];
//					break;
//				}
//			}
//		}
//
//		boolean resultBool = false;
//		if (prpType == null) {
//			resultBool = false;
//		} else {
//			if (prpType instanceof AssociationType) {
//				resultBool = true;
//			} else {
//				resultBool = false;
//			}
//		}
//
//		if (logger.isTraceEnabled()) {
//			logger.trace(MessageFormat.format("isRelationship(). clazz: ''{0}''; fieldName: ''{1}''. return: ", clazz,
//					fieldName, resultBool));
//		}
//
//		return resultBool;
//	}

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
			AssociationAndComponentPathObjPersistenceSupport aacPath = this.associationAndCompositiesMap.get(aacKey);
			return aacPath.getCollType() != null;
		} else {
			return false;
		}
	}

	@Override
	public boolean isManyToOneRelationship(Class<?> ownerClass, String pathFromOwner) {
		AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(ownerClass, pathFromOwner);
		AssociationAndComponentPathObjPersistenceSupport entityAndComponentPath = this.associationAndCompositiesMap.get(aacKey);
		if (entityAndComponentPath != null) {
			return entityAndComponentPath.getRelEntity() != null;
		} else {
			return false;			
		}
	}

//	@Override
//	public abstract Object getIdValue(Class<?> entityClass, Object[] rawKeyValues);
	
	@Override
	public abstract Object getIdValue(Object entityInstanceOrProxy);

	@Override
	public Object getById(Class<?> entityClass, Object idValue) {
		return this.getSessionFactory().getCurrentSession().get(entityClass, (Serializable) idValue);
	}

	@Override
	public AssociationAndComponentPath getAssociationAndComponentOnPath(Class<?> ownerClass, String pathStr) {
		AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(ownerClass, pathStr);
		return this.associationAndCompositiesMap.get(aacKey);
	}

	@Override
	public boolean isComponentByTrack(AssociationAndComponentTrackInfo aacTrackInfo) {
		AssociationAndComponentPathObjPersistenceSupport aacOnPath = this.associationAndCompositiesMap.get(aacTrackInfo);
		if (aacTrackInfo.getEntityAndComponentPath() instanceof AssociationAndComponentPathObjPersistenceSupport) {
			return ((AssociationAndComponentPathObjPersistenceSupport)aacTrackInfo.getEntityAndComponentPath()).getCollType() != null;
		} else {
			throw new RuntimeException("This should not happen. prpType: ");
		}
	}
	
	@Override
	public boolean testCollectionStyle(Class<?> ownerClass, String prpName, CollectionStyle style) {
		ClassMetadata classMetadata = this.persistentClasses.get(ownerClass.getName());
		if (classMetadata != null) {
			Type prpType = classMetadata.getPropertyType(prpName);
			if (prpType instanceof CollectionType) {
				if (style == CollectionStyle.SET && prpType instanceof SetType) {
					return true;
				} else if (style == CollectionStyle.BAG && prpType instanceof BagType) {
					return true;
				} else if (style == CollectionStyle.LIST && prpType instanceof ListType) {
					return true;
				} else if (style == CollectionStyle.MAP && prpType instanceof MapType) {
					return true;
				}
			} else { 
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean isComponentOrRelationship(Class<?> ownerClass, String pathFromOwner) {
		AssociationAndComponentPathKey aacKey = new AssociationAndComponentPathKey(ownerClass, pathFromOwner);
		return this.associationAndCompositiesMap.containsKey(aacKey);
	}
	
	public Object runByReflection(String classStr, String methodName, String[] argsClassStrArr, Object instance, Object[] argsValues) {
		return ReflectionUtil.runByReflection(classStr, methodName, argsClassStrArr, instance, argsValues);
	}
	
	public Object instanciteByReflection(String classStr, String[] argsClassStrArr, Object[] argsValues) {
		return ReflectionUtil.instanciteByReflection(classStr, argsClassStrArr, argsValues);
	}
	
	public Class<?> correctClass(String name) {
		return ReflectionUtil.correctClass(name);
	}
	
	@Override
	public void processNewInstantiate(Class<?> instType, Object instValue) {
		ClassMetadata classMetadata = this.persistentClasses.get(instType.getName());
		if (classMetadata != null) {
			PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(instType);
			for (int i = 0; i < propertyDescriptors.length; i++) {
				PropertyDescriptor propertyDescriptorItem = propertyDescriptors[i];
				if (!("class".equals(propertyDescriptorItem.getName()))) {
					Type prpType = classMetadata.getPropertyType(propertyDescriptorItem.getName());
					Collection resultColl = null;
					if (prpType instanceof CollectionType) {
						if (prpType instanceof SetType) {
							resultColl = new LinkedHashSet<>();
						} else if (prpType instanceof ListType) {
							throw new RuntimeException("Not supported. prpType: " + prpType);
						} else if (prpType instanceof BagType) {
							throw new RuntimeException("Not supported. prpType: " + prpType);
						} else {
							throw new RuntimeException("This should not happen. prpType: " + prpType);
						}
						try {
							PropertyUtils.setProperty(instValue, propertyDescriptorItem.getName(), resultColl);
						} catch (Exception e) {
							throw new RuntimeException("This should not happen. prpType: " + prpType, e);
						}
					} else {
						// non one-to-many
					}
				}
			}
		}		
	}
	
	@Override
	public String getPlayerObjectIdPrpName(Class clazz) {
		return this.persistentClasses.get(clazz.getName()).getIdentifierPropertyName();
	}
	
	@Override
	public void persistenceRemove(Object entity) {
		this.getCurrentSession().delete(entity);		
	}
	
	@Override
	public void persistencePersist(Object entity) {
		this.getCurrentSession().save(entity);		
	}
	
	public Object[] getRawKeyValues(ObjectMapper objectMapper, String stringifiedObjectid) {
		HbObjectIdForStringify hbObjectIdForStringify;
		try {
			hbObjectIdForStringify = objectMapper.readValue(stringifiedObjectid, HbObjectIdForStringify.class);
		} catch (JsonParseException e1) {
			throw new RuntimeException("This should not happen. stringifiedObjectid: " + stringifiedObjectid, e1);
		} catch (JsonMappingException e1) {
			throw new RuntimeException("This should not happen. stringifiedObjectid: " + stringifiedObjectid, e1);
		} catch (IOException e1) {
			throw new RuntimeException("This should not happen. stringifiedObjectid: " + stringifiedObjectid, e1);
		}
		ArrayList<Object> rawObjValueList = new ArrayList();
		for (int i = 0; i < hbObjectIdForStringify.getRawKeyTypeNames().length; i++) {
			Class<?> itemType;
			try {
				itemType = Class.forName(hbObjectIdForStringify.getRawKeyTypeNames()[i]);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("This should not happen", e);
			}
			String itemValueStr = hbObjectIdForStringify.getRawKeyValues()[i]; 
			Method valueOfMethod;
			try {
				if (itemType == String.class) {
					valueOfMethod = null;
				} else {
					valueOfMethod = itemType.getMethod("valueOf", String.class);					
				}
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("This should not happen", e);
			} catch (SecurityException e) {
				throw new RuntimeException("This should not happen", e);
			}
			Object itemValue;
			try {
				if (valueOfMethod == null) {
					itemValue = itemValueStr;
				} else {
					itemValue = valueOfMethod.invoke(null, itemValueStr);					
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException("This should not happen", e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("This should not happen", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("This should not happen", e);
			}
			rawObjValueList.add(itemValue);
		}
		return rawObjValueList.toArray();
	}
	
	@Override
	public String stringfyObjectId(IPlayerManager manager, Object owner) {
		Object[] rawValueArr = null;		
		if (owner instanceof HibernateProxy) {
			rawValueArr = this.getRawKeyValuesFromHbProxy(owner);
		} else {
			rawValueArr = this.getRawKeyValuesFromNonHbProxy(owner);
		}
		
		ArrayList<String> rawValueList = new ArrayList();
		ArrayList<String> rawTypeList = new ArrayList();
		for (Object item : rawValueArr) {
			if (item != null) {
				rawValueList.add(item.toString());
				rawTypeList.add(item.getClass().getName());
			} else {
				rawValueList.add(null);
				rawTypeList.add(null);
			}
		}
		
		HbObjectIdForStringify hbObjectIdForStringify = new HbObjectIdForStringify();		
		hbObjectIdForStringify.setRawKeyValues(new String[rawValueList.size()]);
		hbObjectIdForStringify.setRawKeyTypeNames(new String[rawValueList.size()]);
		hbObjectIdForStringify.setRawKeyValues(rawValueList.toArray(hbObjectIdForStringify.getRawKeyValues()));
		hbObjectIdForStringify.setRawKeyTypeNames(rawTypeList.toArray(hbObjectIdForStringify.getRawKeyTypeNames()));
		
		return manager.getConfig().getObjectMapper().writeValueAsString(hbObjectIdForStringify);
	}
}
