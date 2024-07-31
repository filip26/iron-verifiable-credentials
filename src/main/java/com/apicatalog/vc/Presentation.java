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
public class Presentation extends Verifiable {

    protected URI holder;

    protected Collection<Credential> credentials;

    protected Presentation(ModelVersion version, JsonObject expanded) {
        super(version, expanded);
    }

    @Override
    public boolean isPresentation() {
        return true;
    }

    @Override
    public Presentation asPresentation() {
        return this;
    }

    public Collection<Credential> credentials() {
        return credentials;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#dfn-holders">Holder</a>
     * @return {@link URI} identifying the holder
     */
    public URI holder() {
        return holder;
    }
    
    public Presentation holder(URI holder) {
        this.holder = holder; 
        return this;
    }

    public Presentation credentials(Collection<Credential> credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public void validate() throws DocumentError {
        if (credentials == null || credentials.isEmpty()) {
            throw new DocumentError(ErrorType.Missing, "VerifiableCredentials");
        }
    }

    public static Presentation of(final ModelVersion version, final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Presentation presentation = new Presentation(version, document);

        // @type
        if (!LdNode.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document)) {

            if (LdNode.hasType(document)) {
                throw new DocumentError(ErrorType.Unknown, Term.TYPE);
            }
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        final LdNode node = LdNode.of(document);

        // @id - optional
//        presentation.id(node.id());

        // holder - optional
        presentation.holder(node.node(VcVocab.HOLDER).id());

        return presentation;
    }

    public static boolean isPresentation(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return LdNode.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document);
    }

    @Override
    public URI id() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> type() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> terms() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<Collection<LinkedData>> term(String name) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

}
