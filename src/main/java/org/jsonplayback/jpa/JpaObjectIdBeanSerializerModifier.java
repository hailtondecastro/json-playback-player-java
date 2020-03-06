package org.jsonplayback.jpa;

import java.sql.Blob;
import java.sql.Clob;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsonplayback.player.LazyProperty;
import org.jsonplayback.player.PlayerMetadatas;
import org.jsonplayback.player.implementation.IPlayerManagerImplementor;
import org.jsonplayback.player.implementation.PlayerBeanPropertyWriter;
import org.jsonplayback.player.implementation.PlayerJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.CollectionType;

public class JpaObjectIdBeanSerializerModifier extends BeanSerializerModifier {

	private static Logger logger = LoggerFactory.getLogger(JpaObjectIdBeanSerializerModifier.class);
	private IPlayerManagerImplementor managerImplementor;

	public JpaObjectIdBeanSerializerModifier configManager(IPlayerManagerImplementor manager) {
		this.managerImplementor = manager;
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
		boolean beanClassIsPersistent = this.managerImplementor.isPersistentClass(beanClass);
		String playerObjectIdName = null;
		if (beanClassIsPersistent) {
			playerObjectIdName = this.managerImplementor.getPlayerObjectIdName(beanClass);			
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