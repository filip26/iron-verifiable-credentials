package com.apicatalog.vc.proof;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.builder.GenericTreeCompiler;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.traversal.NodePointer;
import com.apicatalog.linkedtree.traversal.TreeComposer;
import com.apicatalog.linkedtree.writer.NodeDebugWriter;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vcdm.VcdmVocab;

/**
 * An abstract implementation providing partial implementation.
 *
 */
public abstract class VerifiableProof implements Proof {

    protected final Verifiable verifiable;
    protected final CryptoSuite crypto;

    protected URI id;
    protected URI previousProof;

    protected VerificationMethod method;
    protected ProofValue signature;

    protected LinkedFragment ld;

    protected VerifiableProof(Verifiable verifiable, CryptoSuite crypto) {
        this.verifiable = verifiable;
        this.crypto = crypto;
    }

//    /**
//     * Create a generic copy of the verifiable that has no proof attached.
//     * 
//     * @param verifiable
//     * @return
//     * @throws DocumentError 
//     */
//    protected abstract LinkedTree unsigned(LinkedTree verifiable) throws DocumentError;

    /**
     * Create a generic copy of the proof with no proof value, i.e. signature,
     * attached.
     * 
     * @param proof
     * @return
     * @throws
     * @throws
     */
    protected abstract LinkedTree unsignedProof(LinkedTree proof) throws DocumentError;

    @Override
    public void verify(VerificationKey key) throws VerificationError, DocumentError {

        Objects.requireNonNull(signature);
        Objects.requireNonNull(key);

        // a data before issuance - no proof attached
        final LinkedTree unsigned = compose(verifiable);

        Objects.requireNonNull(unsigned);

        // remove a proof value and get a new unsigned copy
        final LinkedTree unsignedProof = unsignedProof(ld.root());

        NodeDebugWriter.writeToStdOut(unsigned);
        NodeDebugWriter.writeToStdOut(unsignedProof);

        // verify signature
        signature.verify(
                crypto,
                unsigned,
                unsignedProof,
                key.publicKey());
    }

    protected LinkedTree compose(Verifiable verifiable) {

        if (verifiable.isCredential()) {
            return verifiable.ld().root();
        }

        if (verifiable.isPresentation()) {

            var x = new TreeComposer(verifiable.ld().root()) {

                GenericTreeCompiler compiler = new GenericTreeCompiler();

                @Override
                protected void end(LinkedNode node, Object[] path) {
                    compiler.accept(node,
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                                    ? (int) path[path.length - 1]
                                    : -1,
                            path.length > 0 && (path[path.length - 1] instanceof String)
                                    ? (String) path[path.length - 1]
                                    : null,
                            path.length);
                }

                @Override
                protected void begin(LinkedNode node, Object[] path) throws TreeBuilderError {
                    compiler.test(node, 
                            path.length > 0 && (path[path.length - 1] instanceof Integer)
                            ? (int) path[path.length - 1]
                            : -1,
                    path.length > 0 && (path[path.length - 1] instanceof String)
                            ? (String) path[path.length - 1]
                            : null,
                    path.length);
                            
                }
            };
            x.inject(NodePointer.of(0, VcdmVocab.VERIFIABLE_CREDENTIALS.uri()),
                    LinkedContainer.EMPTY);

            int index = 0;
            for (Credential credential : verifiable.asPresentation().credentials()) {
                x.inject(NodePointer.of(
                        0,
                        VcdmVocab.VERIFIABLE_CREDENTIALS.uri(),
                        index),
                        credential.ld().root())
                        .inject(NodePointer.of(0, VcdmVocab.VERIFIABLE_CREDENTIALS.uri(), index,
                                VcdmVocab.PROOF.uri()),
                                LinkedContainer.EMPTY);

                int proofIndex = 0;
                for (Proof proof : credential.proofs()) {
                    x.inject(NodePointer.of(0,
                            VcdmVocab.VERIFIABLE_CREDENTIALS.uri(),
                            index,
                            VcdmVocab.PROOF.uri(),
                            proofIndex++),
                            proof.ld().root());
                }
                index++;
            }

            try {
                x.compose();
                return x.compiler.tree();
            } catch (TreeBuilderError e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {
        if (method == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethod");
        }
        if (signature == null) {
            throw new DocumentError(ErrorType.Missing, "ProofValue");
        }
        // TODO ???
//        if (signature.toByteArray() != null &&  signature.toByteArray().length != 32) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValueLength");
//        }
    }

    @Override
    public LinkedNode ld() {
        return ld;
    }

    @Override
    public VerificationMethod method() {
        return method;
    }

    @Override
    public ProofValue signature() {
        return signature;
    }

    @Override
    public URI id() {
        return id;
    }

    @Override
    public URI previousProof() {
        return previousProof;
    }

    @Override
    public CryptoSuite cryptoSuite() {
        return crypto;
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
    }

    protected static void assertEquals(Map<String, Object> params, Term name, String param) throws DocumentError {
        final Object value = params.get(name.name());

        if (value == null) {
            return;
        }

        if (!value.equals(param)) {
            throw new DocumentError(ErrorType.Invalid, name);
        }
    }
}
