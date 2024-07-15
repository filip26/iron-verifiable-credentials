package com.apicatalog.vc.status;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.VcVocab;

public class Status {

    protected final ModelVersion version;

    protected URI id;
    protected Collection<String> type;
    
    protected Status(ModelVersion version) {
        this.version = version;
    }

    public Collection<String> type() {
        return type;
    }
    
    public void validate() throws DocumentError {
        // @type is required when status is present
        if (ModelVersion.V20.equals(version)
                && (type != null
                || type.isEmpty())
                ) {
            throw new DocumentError(ErrorType.Invalid, VcVocab.STATUS);
        }
    }
}
