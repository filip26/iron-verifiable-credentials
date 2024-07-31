package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.oxygen.ld.LinkedData;
import com.apicatalog.vc.model.ModelVersion;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable presentation (VP).
 *
 * @see <a href= "https://www.w3.org/TR/vc-data-model/#presentations">v1.1</a>
 * @see <a href= "https://w3c.github.io/vc-data-model/#presentations">v2.0</a>
 * 
 * @since 0.9.0
 */
public class Presentation2  {

    protected URI holder;

    protected Collection<Credential> credentials;

    protected Presentation2(ModelVersion version) {
//        super(version);
    }
//
//    @Override
//    public boolean isPresentation() {
//        return true;
//    }
//
//    @Override
//    public Presentation2 asPresentation() {
//        return this;
//    }
//
//    public Collection<Credential> credentials() {
//        return credentials;
//    }
//
//    /**
//     * @see <a href="https://www.w3.org/TR/vc-data-model/#dfn-holders">Holder</a>
//     * @return {@link URI} identifying the holder
//     */
//    public URI holder() {
//        return holder;
//    }
//    
//    public Presentation2 holder(URI holder) {
//        this.holder = holder; 
//        return this;
//    }
//
//    public Presentation2 credentials(Collection<Credential> credentials) {
//        this.credentials = credentials;
//        return this;
//    }
//
//    @Override
//    public void validate() throws DocumentError {
//        if (credentials == null || credentials.isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, "VerifiableCredentials");
//        }
//    }
//
//    @Override
//    public URI id() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Collection<String> type() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Collection<String> terms() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Optional<Collection<LinkedData>> term(String name) {
//        // TODO Auto-generated method stub
//        return Optional.empty();
//    }
}
