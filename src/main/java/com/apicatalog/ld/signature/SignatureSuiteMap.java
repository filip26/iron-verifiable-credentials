package com.apicatalog.ld.signature;

import java.util.LinkedHashMap;

public final class SignatureSuiteMap extends LinkedHashMap<String, SignatureSuite> implements SignatureSuiteProvider {

	private static final long serialVersionUID = -8895021841340234772L;

	@Override
	public boolean isSupported(String suiteType) {
		return containsKey(suiteType);
	}

	@Override
	public SignatureSuite getSignatureSuite(String suiteType) {
		return get(suiteType);
	}

	/**
	 * Add a new signature suite. An existing suite of the same type is replaced.
	 * 
	 * @param suite a suite to add
	 * @return the processor instance
	 */
	public SignatureSuiteMap add(final SignatureSuite suite) {
		put(suite.getId().toString(), suite);
		return this;
	}	
}
