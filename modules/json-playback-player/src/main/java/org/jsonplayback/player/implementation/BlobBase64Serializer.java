package org.jsonplayback.player.implementation;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class BlobBase64Serializer extends JsonSerializer<Blob> {

	@Override
	public void serialize(Blob value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		try {
			if (value != null) {
				byte[] byteArrResult = new byte[(int) value.length()];
			
				value.getBinaryStream().read(byteArrResult, 0, (int) value.length());
				gen.writeString(Base64.getEncoder().encodeToString(byteArrResult));
			} else {
				gen.writeNull();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Unexpected", e);
		}		
	}

}
