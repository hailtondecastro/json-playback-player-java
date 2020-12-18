package org.jsonplayback.player;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Alias used to referer to Player Side Type on the Recorder Side.<br>
 * Example (.ts):<br>
 * {@code
 * @RecorderDecorators.playerType({playerType: 'AliasForMasterAEnt'})
 * }
 * @author Hailton de Castro
 *
 */
@Retention(RUNTIME)
@Target({ TYPE })
public @interface RecorderTypeAlias {
	/**
	 * Alias for the java type.
	 * @return
	 */
	String value();
}
