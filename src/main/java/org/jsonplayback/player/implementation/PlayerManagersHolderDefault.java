package org.jsonplayback.player.implementation;

import java.util.HashSet;
import java.util.Set;

import org.jsonplayback.player.IPlayerManager;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;

public class PlayerManagersHolderDefault implements IPlayerManagersHolderImplementor {
	private Set<IPlayerManagerImplementor> managersSet = new HashSet<>();
	
	@Override
	public Set<IPlayerManager> managers() {
		return new HashSet<>(this.managersSet);
	}

	@Override
	public void addManager(IPlayerManager manager) {
		this.managersSet.add((IPlayerManagerImplementor)manager);
	}

	@Override
	public void removeManager(IPlayerManager manager) {
		this.managersSet.remove(manager);
	}

	@Override
	public IPlayerManagerImplementor getStartedManager() {
		return this.getStartedManagerImplementor();
	}
	
	@Override
	public IPlayerManagerImplementor getStartedManagerImplementor() {
		IPlayerManagerImplementor managerResult = null;
		for (IPlayerManagerImplementor manager : this.managersSet) {
			if (manager.isStarted()) {
				if (managerResult != null) {
					throw new RuntimeException("There is more thant one manager started");
				}
				managerResult = manager;
			}
		}
		return managerResult;
	}
	
	@Override
	public boolean thereIsStartedManager() {
		return this.getStartedManager() != null;
	}

	@Override
	public BasicClassIntrospector getClassIntrospertor() {
		return new BasicClassIntrospector() {
			@Override
			public BasicBeanDescription forClassAnnotations(MapperConfig<?> config, JavaType type, MixInResolver r) {
				if (PlayerManagersHolderDefault.this.thereIsStartedManager()) {
					return PlayerManagersHolderDefault.this.getStartedManager().getConfig().getBasicClassIntrospector().forClassAnnotations(config, type, r);
				} else {
					return super.forClassAnnotations(config, type, r);					
				}
			}
			
			@Override
			public BasicBeanDescription forCreation(DeserializationConfig cfg, JavaType type, MixInResolver r) {
				if (PlayerManagersHolderDefault.this.thereIsStartedManager()) {
					return PlayerManagersHolderDefault.this.getStartedManager().getConfig().getBasicClassIntrospector().forCreation(cfg, type, r);
				} else {					
					return super.forCreation(cfg, type, r);
				}
			}
			
			@Override
			public BasicBeanDescription forDeserialization(DeserializationConfig cfg, JavaType type, MixInResolver r) {
				if (PlayerManagersHolderDefault.this.thereIsStartedManager()) {
					return PlayerManagersHolderDefault.this.getStartedManager().getConfig().getBasicClassIntrospector().forDeserialization(cfg, type, r);
				} else {					
					return super.forDeserialization(cfg, type, r);
				}
			}
			
			@Override
			public BasicBeanDescription forDeserializationWithBuilder(DeserializationConfig cfg, JavaType type,
					MixInResolver r) {
				if (PlayerManagersHolderDefault.this.thereIsStartedManager()) {
					return PlayerManagersHolderDefault.this.getStartedManager().getConfig().getBasicClassIntrospector().forDeserializationWithBuilder(cfg, type, r);
				} else {					
					return super.forDeserializationWithBuilder(cfg, type, r);
				}
			}
			
			@Override
			public BasicBeanDescription forDirectClassAnnotations(MapperConfig<?> config, JavaType type,
					MixInResolver r) {
				if (PlayerManagersHolderDefault.this.thereIsStartedManager()) {
					return PlayerManagersHolderDefault.this.getStartedManager().getConfig().getBasicClassIntrospector().forDirectClassAnnotations(config, type, r);
				} else {					
					return super.forDirectClassAnnotations(config, type, r);
				}
			}
			
			@Override
			public BasicBeanDescription forSerialization(SerializationConfig cfg, JavaType type, MixInResolver r) {
				if (PlayerManagersHolderDefault.this.thereIsStartedManager()) {
					return PlayerManagersHolderDefault.this.getStartedManager().getConfig().getBasicClassIntrospector().forSerialization(cfg, type, r);
				} else {					
					return super.forSerialization(cfg, type, r);
				}
			}
		};
	}
}
