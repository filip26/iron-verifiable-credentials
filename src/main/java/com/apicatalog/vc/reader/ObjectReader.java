package com.apicatalog.vc.reader;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.ModelVersion;

/**
 * 
 * @since 0.15.0
 */

public interface ObjectReader<P, R> {

    R read(ModelVersion version, P expanded) throws DocumentError;
}
