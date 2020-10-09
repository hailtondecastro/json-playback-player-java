package org.jsonplayback.player;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PlayerSnapshot<T> {
	private T wrappedSnapshot;

	@SuppressWarnings("unused")
	@JsonIgnore
	private IPlayerManager manager;
	
	public IPlayerManager getManager() {
		return manager;
	}

	@SuppressWarnings("rawtypes")
	public PlayerSnapshot configManager(IPlayerManager manager) {
		this.manager = manager;
		return this;
	}
	
	public PlayerSnapshot(T wrappedSnapshot) {
		super();
		this.wrappedSnapshot = wrappedSnapshot;
	}

	public T getWrappedSnapshot() {
		return wrappedSnapshot;
	}

	public void setWrappedSnapshot(T wrappedSnapshot) {
		this.wrappedSnapshot = wrappedSnapshot;
	}
}
/*gerando conflito*/