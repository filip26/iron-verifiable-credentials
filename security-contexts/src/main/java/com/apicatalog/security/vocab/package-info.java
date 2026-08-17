/**
 * Static registry for W3C Verifiable Credentials Data Integrity JSON-LD
 * security contexts.
 *
 * <h2>Usage</h2>
 * 
 * <pre>{@code
 * // Obtain a context resource by URI
 * ContextResource resource = SecurityContexts.getContext("https://www.w3.org/ns/credentials/v2");
 * 
 * // Verify resource content against its SHA-256 digest
 * if (resource != null && resource.isValid()) {
 *     // Resource integrity confirmed
 * }
 * 
 * // Read context as an InputStream
 * try (InputStream in = SecurityContexts.getContextAsStream("https://www.w3.org/ns/credentials/v2")) {
 *     // Read or parse JSON-LD
 * }
 *
 * // Read context directly as a byte array
 * byte[] bytes = SecurityContexts.getContextAsBytes("https://www.w3.org/ns/credentials/v2");
 *
 * }</pre>
 */
package com.apicatalog.security.vocab;