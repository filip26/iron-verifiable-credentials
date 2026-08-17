/**
 * Static registry for W3C Verifiable Credentials Data Integrity JSON-LD
 * security contexts.
 *
 * <h2>Usage</h2>
 * 
 * <pre>{@code
 * // Retrieve the immutable set of bundled context URIs
 * var uris = SecurityContexts.uris();
 * 
 * // Obtain a context resource by URI
 * ContextResource resource = SecurityContexts.context("https://www.w3.org/ns/credentials/v2");
 * 
 * // Verify resource content against its SHA-256 digest
 * if (resource != null && resource.isValid()) {
 *     // Resource integrity confirmed
 * }
 * 
 * // Read context as an InputStream
 * try (InputStream in = SecurityContexts.contextAsStream("https://www.w3.org/ns/credentials/v2")) {
 *     // Read or parse JSON-LD
 * }
 *
 * // Read context directly as a byte array
 * byte[] bytes = SecurityContexts.contextAsBytes("https://www.w3.org/ns/credentials/v2");
 *
 * // Check for Verifiable Credentials Data Model v2.0 context
 * if (!ctx.isEmpty() && SecurityContext.VCDM_V2.equals(ctx.getFirst())) {
 *     // Handle Verifiable Credentials Data Model v2.0
 * }
 * }</pre>
 */
package com.apicatalog.security.vocab;