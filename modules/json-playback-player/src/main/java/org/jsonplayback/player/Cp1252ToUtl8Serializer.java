package org.jsonplayback.player;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class Cp1252ToUtl8Serializer extends StdSerializer<String> {

	public Cp1252ToUtl8Serializer() {
		super(String.class);
		// TODO Auto-generated constructor stub
	}
	
	public Cp1252ToUtl8Serializer(Class<String> t) {
		super(t);
		// TODO Auto-generated constructor stub
	}

	public Cp1252ToUtl8Serializer(JavaType type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	public Cp1252ToUtl8Serializer(StdSerializer<?> src) {
		super(src);
		// TODO Auto-generated constructor stub
	}

	public Cp1252ToUtl8Serializer(Class<?> t, boolean dummy) {
		super(t, dummy);
	}

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (value != null) {
			gen.writeString(new String(value.getBytes("Windows-1252"), "UTF-8"));			
		} else {
			gen.writeNull();
		}
	}
	
}
