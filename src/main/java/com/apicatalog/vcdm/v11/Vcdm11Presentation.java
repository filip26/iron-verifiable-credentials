package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vcdm.VcdmVerifiable;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

public class Vcdm11Presentation extends VcdmVerifiable implements Presentation {

//    private static final Logger LOGGER = Logger.getLogger(Vcdm11Presentation.class.getName());

    protected URI holder;

    protected Collection<Credential> credentials;

    protected LinkedFragment ld;

    public static Vcdm11Presentation of(LinkedFragment source) throws AdapterError {
        return setup(new Vcdm11Presentation(), source);
    }

    protected static Vcdm11Presentation setup(Vcdm11Presentation presentation, LinkedFragment source) throws AdapterError {
        
        // @id
        presentation.id = source.uri();

        // holder
        presentation.holder = source.uri(VcdmVocab.HOLDER.uri());

        presentation.ld = source;
        return presentation;
    }

    static Collection<Credential> getCredentials(final LinkedContainer tree) throws AdapterError {

        Objects.requireNonNull(tree);

        if (tree.nodes().isEmpty()) {
            return Collections.emptyList();
        }

        var credentials = new ArrayList<Credential>();

        for (final LinkedNode node : tree) {
            credentials.add(getCredential(node.asFragment()));
        }

        return credentials;
    }

    static Credential getCredential(final LinkedFragment fragment) throws AdapterError {

        Objects.requireNonNull(fragment);

        //TODO unknown credentials
        return fragment.type().materialize(Credential.class);
        
//        if (fragment.cast() instanceof Credential credential) {
//            return credential;
//        }

        // FIXME
//        throw new UnsupportedOperationException();
//        return new UnknownProof(fragment);
    }

    @Override
    public LinkedNode ld() {
        return ld;
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
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
        return credentials;
    }

    @Override
    public VcdmVersion version() {
        return VcdmVersion.V20;
    }
}
