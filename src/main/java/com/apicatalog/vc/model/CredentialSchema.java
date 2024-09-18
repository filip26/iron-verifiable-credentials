package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.Linkable;

public interface CredentialSchema extends Linkable {

    URI id();
    Collection<String> type();

}
