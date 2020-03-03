package org.jsonplayback.player.hibernate;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.BagType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ListType;
import org.hibernate.type.SetType;
import org.hibernate.type.Type;
import org.jsonplayback.player.ChangeActionEventArgs;
import org.jsonplayback.player.IChangeActionListener;
import org.jsonplayback.player.IFluentChangeListener;
import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.IReplayable;
import org.jsonplayback.player.Tape;
import org.jsonplayback.player.TapeAction;
import org.jsonplayback.player.util.ReflectionNamesDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

public class ReplayableDefault implements IReplayable {
	private static Logger logger = LoggerFactory.getLogger(ReplayableDefault.class);
	
	private static class ClassPropertyKey {
		private Class entityClass;
		private String propertyName;

		public ClassPropertyKey(Class entityClass, String propertyName) {
			super();
			this.entityClass = entityClass;
			this.propertyName = propertyName;
		}

		public Class getEntityClass() {
			return entityClass;
		}

		public void setEntityClass(Class entityClass) {
			this.entityClass = entityClass;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}

		@Override
		public int hashCode() {
			return 37 * this.entityClass.hashCode() + propertyName.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ClassPropertyKey) {
				ClassPropertyKey classPropertyKeyObj = (ClassPropertyKey) obj;
				return classPropertyKeyObj == this || (this.entityClass.equals(classPropertyKeyObj.getEntityClass())
						&& this.propertyName.equals(classPropertyKeyObj.getPropertyName()));
			} else {
				return false;
			}
		}
	}

	private HashMap<ClassPropertyKey, List<IChangeActionListener>> listenersByPrpsMap = new HashMap<>();
	private HashMap<Class, List<IChangeActionListener>> listenersByClassMap = new HashMap<>();
	private ArrayList<IChangeActionListener> listenersList = new ArrayList<>();
	private Tape tape = null;
	private boolean replayed = false;

	private IPlayerManagerImplementor manager;
	
	public ReplayableDefault configManager(IPlayerManagerImplementor manager) {
		this.manager = manager;
		return this;
	}

	public ReplayableDefault loadPlayback(Tape tape) {
		this.tape = tape;
		return this;
	}

	@Override
	public IReplayable addChangeActionListener(IChangeActionListener changeActionListener) {
		if (changeActionListener == null) {
			throw new IllegalArgumentException("changeActionListener can not be null");
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("addChangeActionListener({0})", changeActionListener.getName()));
		}
		this.listenersList.add(changeActionListener);
		return this;
	}

	@Override
	public <E> IReplayable addChangeActionListenerForClass(Class<E> entClass,
			IChangeActionListener changeActionListener) {
		if (entClass == null) {
			throw new IllegalArgumentException("entClass can not be null");
		}
		if (changeActionListener == null) {
			throw new IllegalArgumentException("changeActionListener can not be null");
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("addChangeActionListener({0}, {1})",
					entClass, changeActionListener.getName()));
		}
		if (!this.listenersByClassMap.containsKey(entClass)) {
			this.listenersByClassMap.put(entClass, new ArrayList<>());
		}
		this.listenersByClassMap.get(entClass).add(changeActionListener);
		return this;
	}

	@Override
	public <E> IReplayable addChangeActionListenerForProperty(Class<E> entClass, Function<E, ?> propertyFunc,
			IChangeActionListener changeActionListener) {
		if (entClass == null) {
			throw new IllegalArgumentException("entClass can not be null");
		}
		if (propertyFunc == null) {
			throw new IllegalArgumentException("propertyFunc can not be null");
		}
		if (changeActionListener == null) {
			throw new IllegalArgumentException("changeActionListener can not be null");
		}
		
		ClassPropertyKey classPropertyKey = new ClassPropertyKey(entClass,
				ReflectionNamesDiscovery.fieldByGetMethod(propertyFunc, entClass));
		
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("addChangeActionListener({0}, {1}, {2})", entClass, classPropertyKey.getPropertyName(), changeActionListener.getName()));
		}
		if (!this.listenersByPrpsMap.containsKey(classPropertyKey)) {
			this.listenersByPrpsMap.put(classPropertyKey, new ArrayList<>());
		}
		this.listenersByPrpsMap.get(classPropertyKey).add(changeActionListener);
		return this;
	}
	
	private void preProcessPlayBack(Tape tape, HashMap<Long, Object> creationRefMap) {
		Session ss = this.manager.getConfig().getSessionFactory().getCurrentSession();
		ObjectMapper objectMapper = this.manager.getConfig().getObjectMapper();
		Collection collection = null;
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("preProcessPlayBack(). tape before pre processing:\n{0}", tape));
		}
		for (TapeAction actionItem : this.tape.getActions()) {
			TapeActionDefault action = (TapeActionDefault) actionItem;
			action.setTapeOwner(this.tape);
			Collection resolvedCollection = action.resolveColletion(objectMapper, this.manager, creationRefMap);
			switch (action.getActionType()) {
			case CREATE:
				if (action.getOwnerCreationId() == null) {
					throw new RuntimeException(MessageFormat.format("ownerCreationId can not be null\naction:\n{0}", action));
				}
				if (action.getOwnerSignatureStr() != null) {
					throw new RuntimeException(MessageFormat.format("originalSignatureStr can not be null\naction:\n{0}", action));
				}
				Object ownerValue = action.resolveOwnerValue(this.manager, creationRefMap);
				
				this.manager.getObjPersistenceSupport().processNewInstantiate(action.getResolvedOwnerPlayerType(), ownerValue);
				
				break;
			case SAVE:
				if (action.getOwnerCreationId() == null) {
					throw new RuntimeException(MessageFormat.format("ownerCreationId can not be null\naction:\n{0}", action));
				}
				if (action.getOwnerSignatureStr() != null) {
					throw new RuntimeException(MessageFormat.format("ownerSignatureStr can not be not null\naction:\n{0}", action));
				}
				
				break;
			case DELETE:
				if (action.getOwnerCreationId() != null) {
					throw new RuntimeException(MessageFormat.format("ownerCreationRefId can not be not null\naction:\n{0}", action));
				}
				if (action.getOwnerSignatureStr() == null) {
					throw new RuntimeException(MessageFormat.format("ownerSignatureStr can not be null\naction:\n{0}", action));
				}
				//nada
				break;
			case COLLECTION_ADD:
				if (action.getFieldName() == null) {
					throw new RuntimeException(MessageFormat.format("fieldName can not be not null\naction:\n{0}", action));
				}
				collection = (Collection) resolvedCollection;
				
				break;
			case COLLECTION_REMOVE:
				if (action.getFieldName() == null) {
					throw new RuntimeException(MessageFormat.format("fieldName can not be not null\naction:\n{0}", action));
				}
				collection = (Collection) resolvedCollection;
				
				break;
			case SET_FIELD:
				if (action.getFieldName() == null) {
					throw new RuntimeException(MessageFormat.format("fieldName can not be not null\naction:\n{0}", action));
				}
				
				break;
			default:
				throw new RuntimeException("This should not happen");
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace(MessageFormat.format("preProcessPlayBack(). tape after pre processing:\n{0}", tape));
		}
	}
	
	@Override
	public void play() {
		if (this.replayed) {
			throw new RuntimeException("Already executed");
		}
		this.replayed = true;
		
		List<IChangeActionListener> actionListenersList = null;
		HashMap<Long, Object> creationRefMap = new HashMap<>();
		Session ss = this.manager.getConfig().getSessionFactory().getCurrentSession();
		ObjectMapper objectMapper = this.manager.getConfig().getObjectMapper();
		
		this.preProcessPlayBack(this.tape, creationRefMap);
		
		for (TapeAction actionItem : this.tape.getActions()) {
			TapeActionDefault action = (TapeActionDefault) actionItem;
			// before events
			Object resolvedOwnerValue = action.resolveOwnerValue(this.manager, creationRefMap);
			String resolvedJavaPropertyName = action.resolveJavaPropertyName(objectMapper, this.manager, creationRefMap);
			@SuppressWarnings("rawtypes")
			Collection resolvedCollection = action.resolveColletion(objectMapper, this.manager, creationRefMap);
			Object resolvedSettedValue = action.resolveSettedValue(objectMapper, this.manager, creationRefMap);
			
			ChangeActionEventArgs<Object> actionEventArgs = new ChangeActionEventArgs<Object>(
					resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection,
					action.getActionType());
			
			if (action.getFieldName() != null) {
				ClassPropertyKey classPropertyKey = new ClassPropertyKey(action.resolveOwnerPlayerType(this.manager, creationRefMap),
						resolvedJavaPropertyName);
				
				actionListenersList = this.listenersByClassMap.get(action.resolveOwnerPlayerType(this.manager, creationRefMap));
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onBeforeChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onBeforeChange(actionEventArgs);
					}
				}
				
				actionListenersList = this.listenersByPrpsMap.get(classPropertyKey);
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onBeforeChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onBeforeChange(actionEventArgs);
					}
				}
				
				actionListenersList = this.listenersList;
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onBeforeChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onBeforeChange(actionEventArgs);
					}
				}
			} else {
				actionListenersList = this.listenersByClassMap.get(action.resolveOwnerPlayerType(this.manager, creationRefMap));
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onBeforeChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onBeforeChange(actionEventArgs);
					}
				}
				
				actionListenersList = this.listenersList;
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onBeforeChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onBeforeChange(actionEventArgs);
					}
				}
			}

			@SuppressWarnings("rawtypes")
			Collection collection = null;
			
			switch (action.getActionType()) {
			case CREATE:
				//nada: feito no preprocessamento		
				break;
			case SAVE:
				ss.save(resolvedOwnerValue);
				break;
			case DELETE:
				ss.delete(resolvedOwnerValue);
				break;
			case COLLECTION_ADD:
				collection = (Collection) resolvedCollection;
				collection.add(resolvedSettedValue);
				
				break;
			case COLLECTION_REMOVE:
				collection = (Collection) resolvedCollection;
				collection.remove(resolvedSettedValue);
				
				break;
			case SET_FIELD:
				try {
					SettableBeanProperty settableBeanProperty = action.resolveBeanDeserializer(objectMapper, this.manager, creationRefMap).findProperty(action.getFieldName()); 
					settableBeanProperty.set(resolvedOwnerValue, resolvedSettedValue);
				} catch (IOException e) {
					throw new RuntimeException(MessageFormat.format("This should not happen.\naction:\n{0}", action));
				}
				break;
			default:
				throw new RuntimeException("This should not happen");
			}

			// after events
			if (action.getFieldName() != null) {
				ClassPropertyKey classPropertyKey = new ClassPropertyKey(action.resolveOwnerPlayerType(this.manager, creationRefMap),
						resolvedJavaPropertyName);

				actionListenersList = this.listenersByPrpsMap.get(classPropertyKey);
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onAfterChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onAfterChange(actionEventArgs);
					}
				}

				actionListenersList = this.listenersByClassMap.get(action.resolveOwnerPlayerType(this.manager, creationRefMap));
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onAfterChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onAfterChange(actionEventArgs);
					}
				}
				
				actionListenersList = this.listenersList;
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onAfterChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onAfterChange(actionEventArgs);
					}
				}
			} else {
				actionListenersList = this.listenersByClassMap.get(action.resolveOwnerPlayerType(this.manager, creationRefMap));
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onAfterChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onAfterChange(actionEventArgs);
					}
				}

				actionListenersList = this.listenersList;
				if (actionListenersList != null) {
					for (IChangeActionListener actionListenerItem : actionListenersList) {
						if (logger.isTraceEnabled()) {
							logger.trace(MessageFormat.format("replay(). running {0}.onAfterChange({1}, {2}, {3}, {4}, {5}) for", actionListenerItem.getName(), resolvedOwnerValue, resolvedSettedValue, resolvedJavaPropertyName, resolvedCollection, action.getActionType()));
						}
						actionListenerItem.onAfterChange(actionEventArgs);
					}
				}
			}
		}
	}

	@Override
	public <E> IFluentChangeListener<E> fluentChangeListener(Class<E> targetClass) {
		// TODO Auto-generated method stub
		return new PlayerFluentChangeListenerDefault<E>(this, targetClass);
	}
	
	public IFluentChangeListener<?> fluentChangeListener() {
		return new PlayerFluentChangeListenerDefault<>(this, null);
	}
}
/*gerando conflito*/