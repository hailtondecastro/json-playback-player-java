package org.jsonplayback.player.spring.context.annotation;

import org.config.jsonplayback.TestServiceConfigBase;
import org.jsonplayback.player.ObjPersistenceMode;
import org.jsonplayback.player.implementation.PlayerManagerDefault;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Quando a property "WA_AMBIENTE" com o valor "DESENV_WORK_STATION".<br>
 * 
 * @author Hailton de Castro
 *
 */
public class OnJpa implements Condition {
	public final static OnJpa INSTANCE = new OnJpa();

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return context.getEnvironment().getProperty(TestServiceConfigBase.OBJ_PERSISTENCE_MODE, ObjPersistenceMode.class, null) == ObjPersistenceMode.JPA;
	}
}
