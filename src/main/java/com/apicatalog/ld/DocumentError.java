package com.apicatalog.ld;

import java.util.Arrays;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;

public class DocumentError extends Exception {

    private static final long serialVersionUID = 2846822739681323437L;

    public enum ErrorType {
        Missing,
        Unknown,
        Invalid,
    }

    private final ErrorType type;

    private final String code;

    public DocumentError(ErrorType type, String code) {
        super(toCode(type, code));
        this.type = type;
        this.code = toCode(type, code);
    }

    public DocumentError(Throwable e, ErrorType type) {
        super(type.name() + "Document", e);
        this.type = type;
        this.code = toCode(type, "Document");
    }

    public DocumentError(Throwable e, ErrorType type, String... code) {
        super(toCode(type, code), e);
        this.type = type;
        this.code = toCode(type, code);
    }

    public DocumentError(ErrorType type, VocabTerm... terms) {
        super(toCode(type, terms));
        this.type = type;
        this.code = toCode(type, terms);
    }

    public DocumentError(Throwable e, ErrorType type, VocabTerm... terms) {
        super(toCode(type, terms), e);
        this.type = type;
        this.code = toCode(type, terms);
    }

    public static DocumentError of(FragmentPropertyError e) {
        return new DocumentError(e, ErrorType.Invalid, e.getPropertyName());
    }

    public ErrorType getType() {
        return type;
    }

    public String code() {
        return code;
    }

    public static void failWithJsonLd(JsonLdError e) throws DocumentError {
        if (JsonLdErrorCode.LOADING_DOCUMENT_FAILED == e.getCode()) {
            throw new DocumentError(e, ErrorType.Invalid);
        }

        if (JsonLdErrorCode.LOADING_REMOTE_CONTEXT_FAILED == e.getCode()) {
            throw new DocumentError(e, ErrorType.Invalid);
        }

        if (JsonLdErrorCode.UNDEFINED_TERM == e.getCode()) {
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    static final String toCode(ErrorType type, VocabTerm... terms) {

        final StringBuilder sb = new StringBuilder();

        if (type != null) {
            sb.append(type.name());
        }

        if (terms == null || terms.length == 0) {
            return sb.append("Document").toString();
        }

        Arrays.stream(terms)
                .forEach(term -> {
                    int index = (term.name().startsWith("@")) ? 1 : 0;

                    sb.append(Character.toUpperCase(term.name().charAt(index)));
                    sb.append(term.name().substring(index + 1));
                });

        return sb.toString();
    }

    static final String toCode(ErrorType type, String... terms) {

        final StringBuilder sb = new StringBuilder();

        if (type != null) {
            sb.append(type.name());
        }

        if (terms == null || terms.length == 0) {
            return sb.append("Document").toString();
        }

        Arrays.stream(terms)
                .forEach(term -> {
                    int index = term.lastIndexOf('#') + 1;
                    if (index == 0) {
                        index = term.lastIndexOf('/') + 1;
                    }
                    if (index == 0) {
                        index = (term.startsWith("@")) ? 1 : 0;
                    }

                    sb.append(Character.toUpperCase(term.charAt(index)));
                    sb.append(term.substring(index + 1));
                });

        return sb.toString();
    }
}
