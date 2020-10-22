package org.jsonplayback.player.spring.context.annotation;

import org.jsonplayback.player.ObjPersistenceMode;
import org.jsonplayback.player.implementation.PlayerManagerDefault;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import config.org.jsonplayback.player.PlayerManagerTestConfig;

/**
 * Quando a property "WA_AMBIENTE" com o valor "DESENV_WORK_STATION".<br>
 * 
 * @author Hailton de Castro
 *
 */
public class OnHibernate4 implements Condition {
	public final static OnHibernate4 INSTANCE = new OnHibernate4();

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return context.getEnvironment().getProperty(PlayerManagerTestConfig.OBJ_PERSISTENCE_MODE, ObjPersistenceMode.class, null) == ObjPersistenceMode.HB4;
	}
}
