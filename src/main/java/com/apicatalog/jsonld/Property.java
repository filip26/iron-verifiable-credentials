package com.apicatalog.jsonld;

import java.net.URI;

public final class Property {

    final PropertyName id;
    final URI type;
    
    final Property parent;
    
    Property(PropertyName id, URI type, Property parent) {
        this.id = id;
        this.type = type;
        this.parent = parent;
    }
    
    public static Property create(String name, String vocabulary, URI type) {
        return create(name, vocabulary, type, null);
    }

    public static Property create(String name, String vocabulary, URI type, Property parent) {
        return new Property(new PropertyName(name, vocabulary), type, parent);
    }

}
