package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.apicatalog.linkedtree.Link;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.lang.LangStringSelector;
import com.apicatalog.linkedtree.lang.LanguageMap;
import com.apicatalog.linkedtree.primitive.GenericFragment;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmVocab;

public class GenericVcdm11Credential implements Vcdm11Credential {

    static final String TYPE = "https://www.w3.org/2018/credentials#VerifiableCredential";

    protected Instant issuanceDate;
    protected Instant expiration;

    protected LinkedContainer subject;

    protected LinkedFragment fragment;
//    protected Link link;
//    protected Collection<String> type;
//    protected Map<String, LinkedContainer> properties;

    protected GenericVcdm11Credential(LinkedFragment fragment) {
        this.fragment = fragment;
    }

    public static GenericVcdm11Credential of(Link id, Collection<String> type, Map<String, LinkedContainer> properties, Supplier<LinkedTree> treeSupplier) {
        return setup(new GenericVcdm11Credential(
                new GenericFragment(id, type, properties, treeSupplier)),
                properties);
    }

    protected static GenericVcdm11Credential setup(GenericVcdm11Credential credential, Map<String, LinkedContainer> properties) {

        credential.expiration = properties.containsKey(VcdmVocab.EXPIRATION_DATE.uri())
                ? properties.get(VcdmVocab.EXPIRATION_DATE.uri())
                        .single(XsdDateTime.class)
                        .datetime()
                : null;

        credential.issuanceDate = properties.containsKey(VcdmVocab.ISSUANCE_DATE.uri())
                ? properties.get(VcdmVocab.ISSUANCE_DATE.uri())
                        .single(XsdDateTime.class)
                        .datetime()
                : null;

        return credential;
    }

//    public LinkedContainer subject() {
//        return properties.get("https://www.w3.org/2018/credentials#credentialSubject");
//    }
//
//    public LinkedFragment issuer() {
//        return properties.containsKey("https://www.w3.org/2018/credentials#issuer")
//                ? properties.get("https://www.w3.org/2018/credentials#issuer")
//                        .single()
//                        .asFragment()
//                : null;
//
//    }

    protected static LangStringSelector getLangMap(Map<String, LinkedContainer> properties, String term) {
        final LinkedContainer container = properties.get(term);
        if (container != null) {
            return LanguageMap.of(container);
        }
        return null;
    }

    @Override
    public Collection<Status> status() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Subject> claims() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Proof> proofs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Instant issuanceDate() {
        return issuanceDate;
    }

    @Override
    public Instant expiration() {
        return expiration;
    }

    @Override
    public LinkedNode ld() {
        return fragment;
    }

    @Override
    public LinkedFragment issuer() {
        return null;
    }

    @Override
    public URI id() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> type() {
        return fragment.type();
    }
}
