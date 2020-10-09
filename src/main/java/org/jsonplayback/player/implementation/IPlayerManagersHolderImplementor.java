package org.jsonplayback.player.implementation;

import java.util.Set;

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
}
