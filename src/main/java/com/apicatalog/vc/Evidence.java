package com.apicatalog.vc;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;
import com.apicatalog.linkedtree.orm.Type;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentError.ErrorType;

@Fragment(generic = true)
public interface Evidence {

    @Id
    URI id();

    @Type
    Collection<String> type();

    /**
     * A custom validation implemented by an ancestor.
     * 
     * @throws DocumentError
     */
    default void validate() throws DocumentError {
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }
    }
}
