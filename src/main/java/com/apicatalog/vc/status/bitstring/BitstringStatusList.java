package com.apicatalog.vc.status.bitstring;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.literal.NumericValue;
import com.apicatalog.linkedtree.orm.Literal;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.vc.Subject;

public class BitstringStatusList implements Subject {

    protected URI id;
    protected Collection<String> type;

    protected Collection<String> purpose;
    protected Bitstring list;
    protected long ttl;
    protected boolean hasClaims;

    protected BitstringStatusList() {
        // protected
    }

    public static BitstringStatusList of(LinkedNode source) throws NodeAdapterError {
        return setup(new BitstringStatusList(), source.asFragment());
    }

    protected static BitstringStatusList setup(BitstringStatusList statusList, LinkedFragment source) throws NodeAdapterError {

        statusList.id = source.uri();
        statusList.type = source.type().stream().toList();

        statusList.purpose = source.collection(
                "https://www.w3.org/ns/credentials/status#statusPurpose",
                String.class,
                l -> l.asLiteral().lexicalValue());

        statusList.list = source.literal(
                "https://www.w3.org/ns/credentials/status#encodedList",
                LinkedLiteral.class,
                Bitstring::of);

        statusList.ttl = source.literal(
                "https://www.w3.org/ns/credentials/status#ttl",
                NumericValue.class,
                n -> n.numberValue().longValue());

        statusList.hasClaims = !source.terms().isEmpty();
        return statusList;
    }

    public Collection<String> type() {
        return type;
    }

    public Collection<String> purpose() {
        return purpose;
    }

    @Term(value =  "encodedList", compact = false)
    @Literal(BitstringAdapter.class)
    public Bitstring list() {
        return list;
    }

    public long ttl() {
        return ttl;
    }

    @Override
    public URI id() {
        return id;
    }

    @Override
    public boolean hasClaims() {
        return hasClaims;
    }
}
