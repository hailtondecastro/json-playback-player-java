package org.jsonplayback.player;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PlayerSnapshot<T> implements IPlayerSnapshot<T> {
	private T wrappedSnapshot;

	@SuppressWarnings("unused")
	@JsonIgnore
	private IPlayerManager manager;
	
	/* (non-Javadoc)
	 * @see org.jsonplayback.player.IPlayerSnapshot#getManager()
	 */
	@Override
	public IPlayerManager getManager() {
		return manager;
	}

	/* (non-Javadoc)
	 * @see org.jsonplayback.player.IPlayerSnapshot#configManager(org.jsonplayback.player.IPlayerManager)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public IPlayerSnapshot<T> configManager(IPlayerManager manager) {
		this.manager = manager;
		return this;
	}
	@JsonIgnore
	private IPlayerConfig overwrittenConfiguration;
	@Override
	public IPlayerSnapshot<T> configOverwrittenConfigurationConfigurationTemporarily(IPlayerConfig config) {
		this.overwrittenConfiguration = config;
		return this;
	}
	@Override
	public IPlayerConfig getOverwrittenConfiguration() {
		return overwrittenConfiguration;
	}

	public PlayerSnapshot(T wrappedSnapshot) {
		super();
		this.wrappedSnapshot = wrappedSnapshot;
	}

	/* (non-Javadoc)
	 * @see org.jsonplayback.player.IPlayerSnapshot#getWrappedSnapshot()
	 */
	@Override
	public T getWrappedSnapshot() {
		return wrappedSnapshot;
	}

	/* (non-Javadoc)
	 * @see org.jsonplayback.player.IPlayerSnapshot#setWrappedSnapshot(T)
	 */
	@Override
	public void setWrappedSnapshot(T wrappedSnapshot) {
		this.wrappedSnapshot = wrappedSnapshot;
	}
}
/*gerando conflito*/