package com.apicatalog.vcdm.io;

import java.util.Collection;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.VerifiableReader;
import com.apicatalog.vc.model.VerifiableReaderProvider;
import com.apicatalog.vc.status.bitstring.BitstringStatusListEntry;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

public class VcdmResolverBuilder  {
//    protected final ProofAdapter proofAdapter;
//    protected final VcdmResolverBuilder resolver;
////    
//    protected VcdmResolverBuilder(final ProofAdapter proofAdapter) {
//        this.proofAdapter = proofAdapter;
//        this.resolver = new VcdmResolverBuilder();
//    }
//
//    public static VerifiableReaderProvider create(final ProofAdapter proofAdapter) {
//
//        resolver.v11(Vcdm11Reader.with(proofAdapter));
//        resolver.v20(Vcdm20Reader.with(proofAdapter, BitstringStatusListEntry.class)
//                // add VCDM 1.1 credential support
//                .v11(resolver.v11().adapter()));
//        return resolver;
//    }
//
//    public VcdmResolverBuilder enable(VcdmVersion version, Class<?> types) {
//
//        switch (version) {
//        case V11:
//            resolver.v11(Vcdm11Reader.with(proofAdapter, types));
//            break;
//        case V20:
//            resolver.v20(Vcdm20Reader.with(proofAdapter, types));
//        }
//        
//        return this;
//    }

}
