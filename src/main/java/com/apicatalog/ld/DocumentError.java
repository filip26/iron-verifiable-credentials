package com.apicatalog.ld;

import java.util.Arrays;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;

public class DocumentError extends Throwable {

    private static final long serialVersionUID = -7146533158378348477L;

    public enum ErrorType {
        Missing,
        Unknown,
        Invalid,
        // Malformed,
    }

    private final ErrorType type;
    private final String code;

    public DocumentError(ErrorType type, String code) {
        super();
        this.type = type;
        this.code = toCode(type, code);
    }

    public DocumentError(Throwable e, ErrorType type, String code) {
        super(e);
        this.type = type;
        this.code = toCode(type, code);
    }

    public DocumentError(ErrorType type, Term... terms) {
        super();
        this.type = type;
        this.code = toCode(type, terms);
    }

    public DocumentError(Throwable e, ErrorType type, Term... terms) {
        super(e);
        this.type = type;
        this.code = toCode(type, terms);
    }

    public ErrorType getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString() + ": " + getCode();
    }

    public String getCode() {
        return code;
    }

    public static void failWithJsonLd(JsonLdError e) throws DocumentError {
        if (JsonLdErrorCode.LOADING_DOCUMENT_FAILED == e.getCode()) {
            throw new DocumentError(e, ErrorType.Invalid);
        }

        if (JsonLdErrorCode.LOADING_REMOTE_CONTEXT_FAILED == e.getCode()) {
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    static final String toCode(ErrorType type, Term... terms) {

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
                    int index = (term.startsWith("@")) ? 1 : 0;

                    sb.append(Character.toUpperCase(term.charAt(index)));
                    sb.append(term.substring(index + 1));
                });

        return sb.toString();
    }
}
