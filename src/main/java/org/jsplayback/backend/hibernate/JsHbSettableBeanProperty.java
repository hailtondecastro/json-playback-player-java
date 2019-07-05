package org.jsplayback.backend.hibernate;

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.Annotations;

public class JsHbSettableBeanProperty extends SettableBeanProperty {
	
	public JsHbSettableBeanProperty(BeanPropertyDefinition propDef, JavaType type, TypeDeserializer typeDeser,
			Annotations contextAnnotations) {
		super(propDef, type, typeDeser, contextAnnotations);
	}

	public JsHbSettableBeanProperty(PropertyName propName, JavaType type, PropertyMetadata metadata,
			JsonDeserializer<Object> valueDeser) {
		super(propName, type, metadata, valueDeser);
		// TODO Auto-generated constructor stub
	}

	public JsHbSettableBeanProperty(PropertyName propName, JavaType type, PropertyName wrapper,
			TypeDeserializer typeDeser, Annotations contextAnnotations, PropertyMetadata metadata) {
		super(propName, type, wrapper, typeDeser, contextAnnotations, metadata);
		// TODO Auto-generated constructor stub
	}

	public JsHbSettableBeanProperty(SettableBeanProperty src, JsonDeserializer<?> deser) {
		super(src, deser);
		// TODO Auto-generated constructor stub
	}

	public JsHbSettableBeanProperty(SettableBeanProperty src, PropertyName newName) {
		super(src, newName);
		// TODO Auto-generated constructor stub
	}

	public JsHbSettableBeanProperty(SettableBeanProperty src) {
		super(src);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SettableBeanProperty withName(PropertyName newName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnnotatedMember getMember() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> acls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(Object instance, Object value) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object setAndReturn(Object instance, Object value) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
/*gerando conflito*/