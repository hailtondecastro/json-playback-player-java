package org.jsonplayback.player.implementation;

import java.util.List;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class PlayerBeanDeserializerModifier extends BeanDeserializerModifier {
	@Override
	public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc,
			List<BeanPropertyDefinition> propDefs) {
		// TODO Auto-generated method stub
		return super.updateProperties(config, beanDesc, propDefs);
	}
	
	@Override
	public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
			JsonDeserializer<?> deserializer) {
		// TODO Auto-generated method stub
		return super.modifyDeserializer(config, beanDesc, deserializer);
	}
}
/*gerando conflito*/