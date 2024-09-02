package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vcdm.VcdmVersion;

public abstract class Vcdm11Verifiable implements Verifiable {

//    private static final Logger LOGGER = Logger.getLogger(Vcdm11Verifiable.class.getName());

    protected URI id;
    protected Collection<Proof> proofs;

    @Override
    public URI id() {
        return id;
    }

    @Override
    public Collection<Proof> proofs() {
        return proofs;
    }
    
    /**
     * Verifiable credentials data model version. Will be moved into a separate
     * interface specialized to VCDM.
     * 
     * @return the data model version, never <code>null</code>
     */
    public VcdmVersion version() {
        return VcdmVersion.V11;
    }


//    /**
//     * Creates a new verifiable instance from the given expanded JSON-LD input.
//     * 
//     * @param version  model version
//     * @param expanded an expanded JSON-LD representing a verifiable
//     * @return materialized verifiable instance
//     * 
//     * @throws DocumentError
//     */
//    public static Verifiable of2(final VcdmVersion version, final JsonObject expanded) throws DocumentError {
//
//        // is a credential?
//        if (JsonLdVcdm11Credential.isCredential(expanded)) {
//            // read the credential object
//            return JsonLdVcdm11Credential.of(version, expanded);
//        }
//
//        // is a presentation?
////        if (JsonLdPresentation.isPresentation(expanded)) {
////            // read the presentation object
////            return JsonLdPresentation.of(version, expanded);
////        }
//
//        // is not expanded JSON-LD object
//        if (JsonUtils.isNull(expanded.get(Keywords.TYPE))) {
//            throw new DocumentError(ErrorType.Missing, Term.TYPE);
//        }
//
//        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
//    }

//
//    protected static <T> Collection<T> readCollection(VcdmVersion version, JsonValue value, ObjectReader<JsonObject, T> reader) throws DocumentError {
//        if (JsonUtils.isNotArray(value)) {
//            return Collections.emptyList();
//        }
//
//        final JsonArray values = value.asJsonArray();
//        final Collection<T> instance = new ArrayList<>(values.size());
//
//        for (final JsonValue item : values) {
//            if (JsonUtils.isNotObject(item)) {
//                // TODO print warning or error? -> processing policy
//                continue;
//            }
//            instance.add(reader.read(version, item.asJsonObject()));
//        }
//        return instance;
//    }
//
//    protected static <T> T readObject(VcdmVersion version, JsonValue value, ObjectReader<JsonObject, T> reader) throws DocumentError {
//        if (JsonUtils.isNotArray(value) || value.asJsonArray().size() != 1) { // TODO throw an error if size > 1?
//            return null;
//        }
//
//        final JsonValue item = value.asJsonArray().get(0);
//        if (JsonUtils.isNotObject(item)) {
//            // TODO print warning or error? -> processing policy
//            return null;
//        }
//        return reader.read(version, item.asJsonObject());
//    }
}
