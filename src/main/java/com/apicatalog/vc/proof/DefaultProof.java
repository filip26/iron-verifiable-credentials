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
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.vc.Verifiable;

/**
 * An abstract implementation providing partial implementation.
 *
 */
public abstract class DefaultProof implements Proof {

    protected final Verifiable verifiable;
    protected final CryptoSuite crypto;

    protected URI id;
    protected URI previousProof;

    protected VerificationMethod method;
    protected ProofValue signature;

    protected LinkedFragment ld;

    protected DefaultProof(Verifiable verifiable, CryptoSuite crypto) {
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
    public void verify(VerificationKey method) throws VerificationError, DocumentError {

        Objects.requireNonNull(signature);
        Objects.requireNonNull(method);

        // a data before issuance - no proof attached
        final LinkedTree unsigned = verifiable.ld().root();

        Objects.requireNonNull(unsigned);

        // remove a proof value and get a new unsigned copy
        final LinkedTree unsignedProof = unsignedProof(ld.root());

        // verify signature
        signature.verify(
                crypto,
                unsigned,
                unsignedProof,
                method.publicKey());
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {
        if (method == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethod");
        }
        if (signature == null) {
            throw new DocumentError(ErrorType.Missing, "ProofValue");
        }
        // FIXME
//        if (value.toByteArray() != null &&  value.to.length != 32) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValueLength");
//        }
//        value.validate();

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
