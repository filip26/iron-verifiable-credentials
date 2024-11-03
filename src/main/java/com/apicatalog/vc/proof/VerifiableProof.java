package com.apicatalog.vc.proof;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.primitive.VerifiableTree;

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
        final LinkedTree unsigned = VerifiableTree.unsigned(verifiable);

        Objects.requireNonNull(unsigned);

        // remove a proof value and get a new unsigned copy
        final LinkedTree unsignedProof = unsignedProof(ld.root());

//        DictionaryWriter.writeToStdOut(unsigned);
//        DictionaryWriter.writeToStdOut(unsignedProof);

        // verify signature
        signature.verify(
                crypto,
                unsigned,
                unsignedProof,
                key.publicKey());
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {
        if (method == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethod");
        }
        if (signature == null) {
            throw new DocumentError(ErrorType.Missing, "ProofValue");
        }
//FIXME        signature.validate();
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
