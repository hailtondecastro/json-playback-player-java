package org.jsonplayback.player.hibernate;

import org.hibernate.proxy.HibernateProxy;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;

public class PlayerBasicClassIntrospector extends BasicClassIntrospector {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public BasicBeanDescription forSerialization(SerializationConfig cfg, JavaType type, MixInResolver r) {
		if (HibernateProxy.class.isAssignableFrom(type.getRawClass())) {
			return super.forSerialization(cfg, type.getSuperClass(), r);
		} else {
			return super.forSerialization(cfg, type, r);
		}
	}
}
/*gerando conflito*/
/*gerando conflito*/