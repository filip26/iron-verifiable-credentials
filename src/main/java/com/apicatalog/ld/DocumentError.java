package com.apicatalog.ld;

import java.util.Arrays;

import com.apicatalog.ld.schema.LdTerm;

public class DocumentError extends Throwable {

    private static final long serialVersionUID = -7146533158378348477L;

    public enum ErrorType {
        // TODO merge Unknown and Invalid?
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
        this.code = type.name().concat(code);
    }

    public DocumentError(Throwable e, ErrorType type, String code) {
        super(e);
        this.type = type;
        this.code = type.name().concat(code);
    }

    public DocumentError(ErrorType type, LdTerm... terms) {
        super();
        this.type = type;
        this.code = toCode(type, terms);
    }

    public DocumentError(Throwable e, ErrorType type, LdTerm... terms) {
        super(e);
        this.type = type;
        this.code = toCode(type, terms);
    }

    public ErrorType getType() {
        return type;
    }

//    public LdTerm[] getTerms() {
//        return terms;
//    }

    public String getCode() {
        return code;
    }

    static final String toCode(ErrorType type, LdTerm... terms) {

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

}
