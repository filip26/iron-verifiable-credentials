package com.apicatalog.vc.reader;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vcdm.VcdmVersion;

/**
 * 
 * @since 0.15.0
 */

public interface ObjectReader<P, R> {

    R read(VcdmVersion version, P expanded) throws DocumentError;
}
