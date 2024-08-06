package com.apicatalog.vc.reader;

import com.apicatalog.oxygen.ld.reader.LinkedDataReader;
import com.apicatalog.vc.Verifiable;

/**
 * Materializes an input representing a verifiable credential or a presentation.
 * 
 * @since 0.15.0
 */
public interface VerifiableReader<O extends Verifiable, I> extends LinkedDataReader<O, I> {

}
