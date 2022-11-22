package com.apicatalog.jsonld;

public final class MissingJsonLdProperty extends Throwable {

    private static final long serialVersionUID = 8374947778594221645L;

    final String property;

    public MissingJsonLdProperty(final String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
