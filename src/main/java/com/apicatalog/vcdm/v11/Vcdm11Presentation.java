package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.Link;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.lang.LangStringSelector;
import com.apicatalog.linkedtree.lang.LanguageMap;
import com.apicatalog.linkedtree.primitive.LinkableObject;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.lt.ObjectFragmentMapper;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.VcdmVocab;

public class Vcdm11Presentation extends Vcdm11Verifiable implements Presentation {

    private static final Logger LOGGER = Logger.getLogger(Vcdm11Presentation.class.getName());

    protected URI holder;

    protected LinkedFragment fragment;

    public static LinkableObject of(
            final Link id,
            final Collection<String> types,
            final Map<String, LinkedContainer> properties,
            final Supplier<LinkedTree> rootSupplier) throws DocumentError {

        var presentation = new Vcdm11Presentation();
        var fragment = new LinkableObject(id, types, properties, rootSupplier, presentation);

        presentation.fragment = fragment;

        var selector = new ObjectFragmentMapper(properties);

        setup(id, types, presentation, selector);

        return fragment;
    }

    protected static LangStringSelector getLangMap(Map<String, LinkedContainer> properties, String term) {
        final LinkedContainer container = properties.get(term);
        if (container != null) {
            return LanguageMap.of(container);
        }
        return null;
    }

    protected static void setup(final Link id, final Collection<String> types, Vcdm11Presentation presentation, final ObjectFragmentMapper selector) throws DocumentError {
        // @id
        presentation.id = selector.id(id);
        
        // holder
        presentation.holder = selector.id(VcdmVocab.HOLDER);
    
        // credentials
//        credential.credentials = selector.fragments(VcdmVocab.VERIFIABLE_CREDENTIALS);
        
        // proofs
        if (selector.properties().containsKey(VcdmVocab.PROOF.uri())) {
            presentation.proofs = EmbeddedProof.getProofs(
                    selector.properties().get(VcdmVocab.PROOF.uri()).asContainer());
        }
    }

    @Override
    public LinkedNode ld() {
        return fragment;
    }

    @Override
    public Collection<String> type() {
        return fragment.type();
    }

    @Override
    public void validate() throws DocumentError {

        // @type - mandatory
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }

    }

    @Override
    public URI holder() {
        return holder;
    }

    @Override
    public Collection<Credential> credentials() {
        // TODO Auto-generated method stub
        return null;
    }
    
    //
//  } else if (verifiable.isPresentation()) {
//  //
//  // // verify presentation proofs
//  // verifiable.proofs(readProofs(context, expanded, loader));
//  //
//  // final Collection<Credential> credentials = new ArrayList<>();
//  //
//  //// for (final JsonObject presentedCredentials :
//  // VerifiableReader.getCredentials(expanded)) {
//  ////
//  //// if (!VerifiableReader.isCredential(presentedCredentials)) {
//  //// throw new DocumentError(ErrorType.Invalid, VcVocab.VERIFIABLE_CREDENTIALS,
//  // Term.TYPE);
//  //// }
//  ////// var params = new HashMap<>();
//  ////// FIXME credentials.add(verifyExpanded(version, context,
//  // presentedCredentials, params, loader).asCredential());
//  //// }
//  //
//  // ((JsonLdPresentation)verifiable.asPresentation()).credentials(credentials);
//  //
//  return verifiable;
//}
//    protected Collection<Proof> readProofs(JsonStructure context, JsonObject expanded, DocumentLoader loader) throws DocumentError {
//
//        // get proofs - throws an exception if there is no proof, never null nor an
//        // empty collection
//        final Collection<JsonObject> expandedProofs = EmbeddedProof.assertProof(expanded);
//
//        // a data before issuance - no proof attached
//        final JsonObject unsigned = EmbeddedProof.removeProofs(expanded);
//
//        final Collection<Proof> proofs = new ArrayList<>(expandedProofs.size());
//
//        // read attached proofs
//        for (final JsonObject expandedProof : expandedProofs) {
//
//            final Collection<String> proofTypes = LdType.strings(expandedProof);
//
//            if (proofTypes == null || proofTypes.isEmpty()) {
//                throw new DocumentError(ErrorType.Missing, VcdmVocab.PROOF, Term.TYPE);
//            }
//
//            final SignatureSuite signatureSuite = findSuite(proofTypes, expandedProof);
//
//            Proof proof = null;
//
//            if (signatureSuite != null) {
////                proof = signatureSuite.getProof(expandedProof, loader);
//            }
//
//            if (proof == null) {
////                if (failOnUnsupportedProof) {
////                    throw new VerificationError(Code.UnsupportedCryptoSuite);
////                }
////FIXME                proof = new UnknownProof(expandedProof);
//            }
//
//            proofs.add(proof);
//        }
//        return proofs;
//    }

//    protected SignatureSuite findSuite(Collection<String> proofTypes, JsonObject expandedProof) {
//        for (final SignatureSuite suite : suites) {
//            for (final String proofType : proofTypes) {
////                if (suite.isSupported(proofType, expandedProof)) {
////                    return suite;
////                }
//            }
//        }
//        return null;
//    }

}
