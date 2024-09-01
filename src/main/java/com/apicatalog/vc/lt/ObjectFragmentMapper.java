package com.apicatalog.vc.lt;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.linkedtree.Link;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedNode;

//????
public record ObjectFragmentMapper(
        Map<String, LinkedContainer> properties) {

    public Linkable single(Term term) throws DocumentError {

        Objects.requireNonNull(term);

        final LinkedContainer container = properties.getOrDefault(term.uri(), LinkedContainer.EMPTY);

        if (container.nodes().isEmpty()) {
            return null;
        }

        if (container.nodes().size() != 1) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        try {
            final LinkedNode node = container.single();

            if (node == null) {
                return null;
            }

            if (node.isFragment()) {
                return node.asFragment().cast();

            } else if (node.isLiteral()) {
                return node.asLiteral().cast();
            }
            throw new DocumentError(ErrorType.Invalid, term);

        } catch (ClassCastException e) {
            throw new DocumentError(e, ErrorType.Invalid, term);
        }
    }

    public URI id(Link link) throws DocumentError {
        if (link == null) {
            return null;
        }
        try {
            final String uri = link.uri();
            if (uri != null) {
                return URI.create(uri);
            }
        } catch (IllegalArgumentException e) {

        }
        throw new DocumentError(ErrorType.Invalid, Keywords.ID);
    }

    public URI id(Term term) throws DocumentError {
        try {
            return single(term, node -> {
                if (node.ld().isFragment()) {
                    final String uri = node.ld().asFragment().id().uri();
                    if (uri != null) {
                        return URI.create(uri);
                    }
                }
                return null;
            });
        } catch (IllegalArgumentException e) {
            throw new DocumentError(ErrorType.Invalid, term);
        }
    }

    public <R> R single(Term term, Function<Linkable, R> mapper) throws DocumentError {

        Objects.requireNonNull(term);

        final LinkedContainer container = properties.getOrDefault(term.uri(), LinkedContainer.EMPTY);

        if (container.nodes().isEmpty()) {
            return null;
        }

        if (container.nodes().size() != 1) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        try {
            final LinkedNode node = container.single();

            if (node == null) {
                return null;
            }

            Linkable value = null;

            if (node.isFragment()) {
                value = node.asFragment().cast();

            } else if (node.isLiteral()) {
                value = node.asLiteral();

            } else {
                throw new DocumentError(ErrorType.Invalid, term);
            }

            return mapper.apply(value);

        } catch (DocumentError e) {
            throw e;
        } catch (Exception e) {
            throw new DocumentError(e, ErrorType.Invalid, term);
        }
    }

    public <T extends Linkable> T single(Term term, Class<T> clazz) throws DocumentError {

        Objects.requireNonNull(clazz);

        final LinkedContainer container = properties.getOrDefault(term.uri(), LinkedContainer.EMPTY);

        if (container.nodes().isEmpty()) {
            return null;
        }

        if (container.nodes().size() != 1) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        try {
            final LinkedNode node = container.single();

            if (node == null) {
                return null;
            }

            T value = null;

            if (node.isFragment()) {
                value = node.asFragment().cast(clazz);
            } else if (node.isLiteral()) {
                value = node.asLiteral().cast(clazz);
            } else {
                throw new DocumentError(ErrorType.Invalid, term);
            }

            return value;

        } catch (ClassCastException e) {
            throw new DocumentError(e, ErrorType.Invalid, term);
        }
    }

    public <T extends Linkable, R> R single(Term term, Class<T> clazz, Function<T, R> mapper) throws DocumentError {

        Objects.requireNonNull(mapper);

        try {
            T value = single(term, clazz);

            if (value == null) {
                return null;
            }
            return mapper.apply(value);

        } catch (DocumentError e) {
            throw e;
        } catch (Exception e) {
            throw new DocumentError(e, ErrorType.Invalid, term);
        }
    }

}
