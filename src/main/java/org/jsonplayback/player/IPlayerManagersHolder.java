package org.jsonplayback.player;

import java.util.Set;

/**
 * Support for two or more Managers.
 * 
 * @author Hailton de Castro
 *
 */
public interface IPlayerManagersHolder {
	Set<IPlayerManager> managers();
	void addManager(IPlayerManager manager);
	void removeManager(IPlayerManager manager);
	IPlayerManager getStartedManager();
	boolean thereIsStartedManager();
	IReplayable prepareReplayable(Tape tape);
	IPlayerManager resolveManagerBySignature(String signature);
}
