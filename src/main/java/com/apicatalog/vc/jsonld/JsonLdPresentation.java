package com.apicatalog.vc.jsonld;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class JsonLdPresentation extends JsonLdVerifiable implements Presentation {

    protected URI holder;

    protected Collection<Credential> credentials;

    protected JsonLdPresentation(VcdmVersion version, JsonObject expanded) {
        super(version, expanded);
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
    
    public JsonLdPresentation holder(URI holder) {
        this.holder = holder; 
        return this;
    }

    public JsonLdPresentation credentials(Collection<Credential> credentials) {
        this.credentials = credentials;
        return this;
    }

    public static JsonLdPresentation of(final VcdmVersion version, final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final JsonLdPresentation presentation = new JsonLdPresentation(version, document);

        // @type
        if (!LdNode.isTypeOf(VcdmVocab.PRESENTATION_TYPE.uri(), document)) {

            if (LdNode.hasType(document)) {
                throw new DocumentError(ErrorType.Unknown, Term.TYPE);
            }
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

//        final LdNode node = LdNode.of(document);

        // @id - optional
//        presentation.id(node.id());

        // holder - optional
//        presentation.holder(node.node(VcVocab.HOLDER).id());

        return presentation;
    }

    public static boolean isPresentation(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return LdNode.isTypeOf(VcdmVocab.PRESENTATION_TYPE.uri(), document);
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
    public Collection<Proof> proofs() {
        // TODO Auto-generated method stub
        return null;
    }

//    @Override
//    public VcdmVersion version() {
//        // TODO Auto-generated method stub
//        return null;
//    }
}
