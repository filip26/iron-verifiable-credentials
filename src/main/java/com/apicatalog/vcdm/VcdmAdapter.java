package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.jsonld.JsonLdVerifiableAdapter;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.v11.reader.Vcdm11Reader;

public class VcdmAdapter implements JsonLdVerifiableAdapter {

    protected final SignatureSuite[] suites;

    protected boolean v11;
    protected boolean v20;

    public VcdmAdapter(SignatureSuite[] suites) {
        this.suites = suites;

        // defaults
        this.v11 = true;
        this.v20 = true;
    }

    @Override
    public VerifiableReader reader(final Collection<String> contexts) throws DocumentError {

        if (contexts == null || contexts.isEmpty()) {
            return null;
        }

        return switch (getVersion(contexts)) {
        case V11 -> v11 ? new Vcdm11Reader(suites) : null;
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

    public void v11(boolean enabled) {
        this.v11 = enabled;
    }

    public void v20(boolean enabled) {
        this.v20 = enabled;
    }
}
