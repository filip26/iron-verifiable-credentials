package com.apicatalog.vcdm.io;

import java.util.Collection;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.reader.ReaderResolver;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

public class VcdmResolver implements ReaderResolver {

    protected Vcdm11Reader v11;
    protected Vcdm20Reader v20;

    public VcdmResolver() {
        this.v11 = null;
        this.v20 = null;
    }

    @Override
    public VerifiableReader resolveReader(final Collection<String> contexts) throws DocumentError {

        if (contexts == null || contexts.isEmpty()) {
            return null;
        }

        return switch (getVersion(contexts)) {
        case V11 -> v11;
        case V20 -> v20;
        default -> null;
        };
    }

    public static VcdmVersion getVersion(final Collection<String> contexts) throws DocumentError {

        if (contexts == null || contexts.isEmpty()) {
            return null;
        }

        final String firstContext = contexts.iterator().next();

        if (VcdmVocab.CONTEXT_MODEL_V1.equals(firstContext)) {
            return VcdmVersion.V11;
        }
        if (VcdmVocab.CONTEXT_MODEL_V2.equals(firstContext)) {
            return VcdmVersion.V20;
        }

        for (final String context : contexts) {
            if (VcdmVocab.CONTEXT_MODEL_V1.equals(context)
                    || VcdmVocab.CONTEXT_MODEL_V2.equals(context)) {

                throw new DocumentError(ErrorType.Invalid, Keywords.CONTEXT);
            }
        }
        return null;
    }

    public void v11(Vcdm11Reader v11) {
        this.v11 = v11;
    }
    
    public void v20(Vcdm20Reader v20) {
        this.v20 = v20;
    }
}