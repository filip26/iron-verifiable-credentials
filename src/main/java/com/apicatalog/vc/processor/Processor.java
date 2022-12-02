package com.apicatalog.vc.processor;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdTerm;

import jakarta.json.JsonObject;

abstract class Processor<T extends Processor<?>> {

	protected DocumentLoader loader;
	protected boolean bundledContexts;
	protected URI base;

	protected Processor() {
		// default values
		this.loader = null;
		this.bundledContexts = true;
		this.base = null;
	}

	@SuppressWarnings("unchecked")
	public T loader(DocumentLoader loader) {
		this.loader = loader;
		return (T) this;
	}

	/**
	 * Use well-known contexts that are bundled with the library instead of fetching
	 * it online. <code>true</code> by default. Disabling might cause slower
	 * processing.
	 *
	 * @param enable
	 * @return the processor instance
	 */
	@SuppressWarnings("unchecked")
	public T useBundledContexts(boolean enable) {
		this.bundledContexts = enable;
		return (T) this;
	}

	/**
	 * If set, this overrides the input document's IRI.
	 *
	 * @param base
	 * @return the processor instance
	 */
	@SuppressWarnings("unchecked")
	public T base(URI base) {
		this.base = base;
		return (T) this;
	}

	protected static Verifiable get(final JsonObject expanded) throws DocumentError {

		// is a credential?
		if (Credential.isCredential(expanded)) {
			// validate the credential object
			return Credential.from(expanded);
		}

		// is a presentation?
		if (Presentation.isPresentation(expanded)) {
			// validate the presentation object
			return Presentation.from(expanded);
		}

		// is not expanded JSON-LD object
		if (!JsonLdReader.hasType(expanded)) {
			throw new DocumentError(ErrorType.Missing, LdTerm.TYPE);
		}

		throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
	}

	protected void failWithJsonLd(JsonLdError e) throws DocumentError {
		if (JsonLdErrorCode.LOADING_DOCUMENT_FAILED == e.getCode()) {
			throw new DocumentError(e, ErrorType.Invalid);
		}

		if (JsonLdErrorCode.LOADING_REMOTE_CONTEXT_FAILED == e.getCode()) {
			throw new DocumentError(e, ErrorType.Invalid);
		}
	}
}
