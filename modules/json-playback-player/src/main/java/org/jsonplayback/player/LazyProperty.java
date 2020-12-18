package org.jsonplayback.player;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface LazyProperty {
	/**
	 * If <code>true</code>,
	 * {@link IPlayerManager#needDirectWrite(SignatureBean)}
	 * must be used to retrieve the content on the {@link OutputStream}.
	 * 
	 * @return
	 */
	boolean directRawWrite() default false;
	
	/**
	 * Default "application/octet-stream"
	 * 
	 * @return
	 */
	String contentTypePrefix() default "application/octet-stream";
	
	/**
	 * Default "utf-8"
	 * 
	 * @return
	 */
	String charset() default "utf-8";
	
	/**
	 * Default 1024
	 * @return
	 */
	int bufferSize() default 1024;
	
	/**
	 * 
	 */
	int nonLazyMaxSize() default 0;
}
