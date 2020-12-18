package org.jsonplayback.player.implementation;

import java.io.IOException;

import org.jsonplayback.player.IPlayerSnapshot;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PlayerSnapshotSerializer extends JsonSerializer<IPlayerSnapshot> {

	public PlayerSnapshotSerializer(){
		
	}
	
	private IPlayerManagersHolderImplementor managersHolder;
	
	public PlayerSnapshotSerializer  configManagerHolder(IPlayerManagersHolderImplementor managersHolder) {
		this.managersHolder = managersHolder;
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void serialize(IPlayerSnapshot value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		try {
			if(value.getOverwrittenConfiguration() != null) {
				value.getManager().overwriteConfigurationTemporarily(value.getOverwrittenConfiguration());
			}
			value.getManager().startJsonWriteIntersept();

			final JsonSerializer<Object> defaultJsonSerializer = serializers.findValueSerializer(Object.class);
			
			gen.writeStartObject();
			gen.writeFieldName("wrappedSnapshot");
			serializers.findValueSerializer(value.getWrappedSnapshot().getClass()).serialize(value.getWrappedSnapshot(), gen, serializers);
			gen.writeEndObject();
		} finally {
			value.getManager().stopJsonWriteIntersept();
		}
	}
}
/*gerando conflito*/