package com.apicatalog.jsonld;

import jakarta.json.JsonValue;

public final class InvalidJsonLdValue extends Throwable {

	private static final long serialVersionUID = 530678852247721905L;

	final String property;
	final JsonValue value;
	
	public InvalidJsonLdValue(final String property, final JsonValue value, final String message) {
		super(message);
		this.property = property;
		this.value = value;
	}	
	
	public JsonValue getValue() {
		return value;
	}
	
	public String getProperty() {
		return property;
	}
}
