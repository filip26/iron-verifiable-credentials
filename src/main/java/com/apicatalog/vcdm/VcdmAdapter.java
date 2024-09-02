package com.apicatalog.vcdm;

import java.net.URI;
import java.util.Collection;
import java.util.logging.Logger;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.jsonld.JsonLdVerifiableAdapter;
import com.apicatalog.vc.jsonld.JsonLdVerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.v11.reader.Vcdm11Reader;

public class VcdmAdapter implements JsonLdVerifiableAdapter {

    private static final Logger LOGGER = Logger.getLogger(VcdmAdapter.class.getName());

    protected final SignatureSuite[] suites;

    protected URI base;
    
    public VcdmAdapter(SignatureSuite[] suites, URI base) {
        this.suites = suites;
        this.base = base;
    }
    
    @Override
    public JsonLdVerifiableReader reader(final Collection<String> contexts) throws DocumentError {

        if (contexts == null || contexts.isEmpty()) {
            return null;
        }

        return switch (getVersion(contexts)) {
            case V11 -> new Vcdm11Reader(suites);
            case V20 -> null;
            default -> null;
        };
    }

    public static VcdmVersion getVersion(final Collection<String> contexts) throws DocumentError {

        if (contexts == null || contexts.isEmpty()) {
            return null;
        }

        final String firstContext = contexts.iterator().next();
        if ("https://www.w3.org/2018/credentials/v1".equals(firstContext)) {
            return VcdmVersion.V11;
        }
        if ("https://www.w3.org/ns/credentials/v2".equals(firstContext)) {
            return VcdmVersion.V20;
        }

        for (final String context : contexts) {
            if ("https://www.w3.org/2018/credentials/v1".equals(context)
                    || "https://www.w3.org/ns/credentials/v2".equals(context)) {

                throw new DocumentError(ErrorType.Invalid, Keywords.CONTEXT);
            }
        }
        return null;
    }
}
