package org.jsonplayback.player;

public interface IPlayerSnapshot<T> {

	IPlayerManager getManager();

	IPlayerSnapshot<T> configManager(IPlayerManager manager);

	T getWrappedSnapshot();

	void setWrappedSnapshot(T wrappedSnapshot);

	IPlayerSnapshot<T> configOverwrittenConfigurationConfigurationTemporarily(IPlayerConfig config);

	IPlayerConfig getOverwrittenConfiguration();

}