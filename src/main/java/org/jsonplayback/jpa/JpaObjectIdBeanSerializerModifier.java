package org.jsonplayback.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsonplayback.player.implementation.IPlayerManagerImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class JpaObjectIdBeanSerializerModifier extends BeanSerializerModifier {

	private static Logger logger = LoggerFactory.getLogger(JpaObjectIdBeanSerializerModifier.class);
	IPlayerManagerImplementor manager;

	public JpaObjectIdBeanSerializerModifier configManager(IPlayerManagerImplementor manager) {
		this.manager = manager;
		return this;
	}

	@Override
	public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
			List<BeanPropertyWriter> beanProperties) {
		List<BeanPropertyWriter> newBeanPropertyWriterList = new ArrayList<BeanPropertyWriter>();

		Map<String, BeanPropertyDefinition> prpDefsMap = new HashMap<>();
		for (BeanPropertyDefinition beanPropertyDefinition : beanDesc.findProperties()) {
			prpDefsMap.put(beanPropertyDefinition.getName(), beanPropertyDefinition);
		}

		Class beanClass = beanDesc.getType().getRawClass();
		Class unwrappedBeanClass = this.manager.getConfig().getObjPersistenceSupport().unwrappRealType(beanClass);
		boolean beanClassIsPersistent = this.manager.isPersistentClass(unwrappedBeanClass);
		String playerObjectIdName = null;
		if (beanClassIsPersistent) {
			playerObjectIdName = this.manager.getPlayerObjectIdName(unwrappedBeanClass);			
		}
		
		for (int i = 0; i < beanProperties.size(); i++) {
			BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);
			BeanPropertyDefinition prpDef = prpDefsMap.get(beanPropertyWriter.getName());
			if (beanClassIsPersistent) {
				if (playerObjectIdName.equals(beanPropertyWriter.getName())) {
					newBeanPropertyWriterList.add(beanPropertyWriter);
				}
			} else {
				newBeanPropertyWriterList.add(beanPropertyWriter);
			}
		}
			

		return newBeanPropertyWriterList;
	}
}