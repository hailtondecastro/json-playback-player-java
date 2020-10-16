package org.jsonplayback.player.implementation;

import org.jsonplayback.player.IPlayerManagersHolder;

import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;

/**
 * Support for two or more Managers.
 * 
 * @author Hailton de Castro
 *
 */
public interface IPlayerManagersHolderImplementor extends IPlayerManagersHolder {
	IPlayerManagerImplementor getStartedManagerImplementor();
	BasicClassIntrospector getClassIntrospertor();
	IPlayerManagerImplementor resolveManagerBySignature(String signature);
	IPlayerManagerImplementor resolveManagerByType(String typeNameOrAlias);
	IPlayerManagerImplementor resolveManagerByType(Class<?> type);
}
