package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Id;
import com.apicatalog.linkedtree.orm.Type;

@Fragment(generic = true)
public interface CredentialSchema {

    @Id
    URI id();

    @Type
    Collection<String> type();

}
