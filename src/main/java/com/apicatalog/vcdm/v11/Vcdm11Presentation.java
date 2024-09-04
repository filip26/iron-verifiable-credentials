package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
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

    protected Collection<Credential> credentials;

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
        if (selector.properties().containsKey(VcdmVocab.VERIFIABLE_CREDENTIALS.uri())) {
            presentation.credentials = getCredentials(
                    selector
                            .properties()
                            .get(VcdmVocab.VERIFIABLE_CREDENTIALS.uri())
                            .asContainer());
        }

        // proofs
        if (selector.properties().containsKey(VcdmVocab.PROOF.uri())) {
            presentation.proofs = EmbeddedProof.getProofs(
                    selector.properties().get(VcdmVocab.PROOF.uri()).asContainer());
        }
    }

    static Collection<Credential> getCredentials(final LinkedContainer tree) {

        Objects.requireNonNull(tree);

        if (tree.nodes().isEmpty()) {
            return Collections.emptyList();
        }

        var credentials = new ArrayList<Credential>();

        for (final LinkedNode node : tree) {
            if (node.isTree()) {
                credentials.add(getCredential(node.asTree().single().asFragment()));
                continue;
            }
            credentials.add(getCredential(node.asFragment()));
        }

        return credentials;
    }

    static Credential getCredential(final LinkedFragment fragment) {

        Objects.requireNonNull(fragment);

        if (fragment.cast() instanceof Credential credential) {
            return credential;
        }

        // FIXME
        throw new UnsupportedOperationException();
//        return new UnknownProof(fragment);
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
        return credentials;
    }
}
