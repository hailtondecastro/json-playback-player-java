package org.jsonplayback.player.spring.context.annotation;

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
public class OnCustomizedPersistence implements Condition {
	public final static OnCustomizedPersistence INSTANCE = new OnCustomizedPersistence();

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return PlayerManagerDefault.getObjPersistenceModeStatic() == ObjPersistenceMode.CUSTOMIZED_PERSISTENCE;
	}
}
