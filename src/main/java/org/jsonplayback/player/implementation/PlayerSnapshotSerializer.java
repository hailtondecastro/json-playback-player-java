package org.jsonplayback.player.implementation;

import java.io.IOException;

import org.jsonplayback.player.IPlayerManager;
import org.jsonplayback.player.PlayerSnapshot;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PlayerSnapshotSerializer extends JsonSerializer<PlayerSnapshot> {

	public PlayerSnapshotSerializer(){
		
	}
	
	private IPlayerManager manager;
	
	public PlayerSnapshotSerializer configManager(IPlayerManager manager) {
		this.manager = manager;
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void serialize(PlayerSnapshot value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		try {
			this.manager.startJsonWriteIntersept();

			final JsonSerializer<Object> defaultJsonSerializer = serializers.findValueSerializer(Object.class);
			
			gen.writeStartObject();
			gen.writeFieldName("wrappedSnapshot");
			serializers.findValueSerializer(value.getWrappedSnapshot().getClass()).serialize(value.getWrappedSnapshot(), gen, serializers);
			gen.writeEndObject();
		} finally {
			this.manager.stopJsonWriteIntersept();
		}
	}
}
/*gerando conflito*/