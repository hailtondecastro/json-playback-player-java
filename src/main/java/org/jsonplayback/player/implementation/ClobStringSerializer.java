package org.jsonplayback.player.hibernate;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.sql.Clob;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ClobStringSerializer extends JsonSerializer<Clob> {

	@Override
	public void serialize(Clob value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		try {
			Reader r = value.getCharacterStream();
			if (value != null) {			
				CharBuffer cbuffer = CharBuffer.allocate(10);
				int lastReadedCount = 0;
				StringBuilder sb = new StringBuilder();
				do {
					lastReadedCount = r.read(cbuffer);
					if (lastReadedCount > 0) {
						cbuffer.flip();
						sb.append(cbuffer.toString());
					}
				} while (lastReadedCount > 0);
				
				gen.writeString(sb.toString());
			} else {
				gen.writeNull();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Unexpected", e);
		}		
	}
}
