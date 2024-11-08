package com.apicatalog.vc.status.bitstring;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.literal.NumericValue;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.status.Status;

@Fragment
@Vocab("https://www.w3.org/ns/credentials/status#")
public class DeprecatedBitstringStatusListEntry implements Status {

    protected URI id;
    
    protected Collection<String> type;
    
    protected String purpose;

    protected long index;

    protected URI credential;

    protected int indexBitLength;

    protected BitstringStatusListMessages messages;
    
    protected Collection<URI> references;
    
    protected DeprecatedBitstringStatusListEntry() {
        // protected
    }

    public static DeprecatedBitstringStatusListEntry of(LinkedFragment source) throws NodeAdapterError {
        var entry = new DeprecatedBitstringStatusListEntry();
        return setup(entry, source);
    }

    protected static DeprecatedBitstringStatusListEntry setup(DeprecatedBitstringStatusListEntry entry, LinkedFragment source) throws NodeAdapterError {

        entry.id = source.uri();
        entry.type = source.type().stream().toList();

        entry.references = source.collection(
                "https://www.w3.org/ns/credentials/examples#statusReference", //FIXME
                URI.class,
                l -> URI.create(l.asLiteral().lexicalValue()));

//        statusList.list = source.literal(
//                "https://www.w3.org/ns/credentials/status#encodedList",
//                LinkedLiteral.class,
//                Bitstring::of);

        entry.purpose = source.literal(
                "https://www.w3.org/ns/credentials/status#statusPurpose",
                LinkedLiteral.class,
                LinkedLiteral::lexicalValue);

        entry.index = source.literal(
                "https://www.w3.org/ns/credentials/status#statusListIndex",
                LinkedLiteral.class,
                l -> Long.valueOf(l.lexicalValue()));

        entry.indexBitLength = source.literal(
                "https://www.w3.org/ns/credentials/examples#statusSize",    //FIXME
                NumericValue.class,
                n -> n.numberValue().intValue(),
                1);

//        entry.validFrom = source.xsdDateTime(VcdmVocab.VALID_FROM.uri());
//        entry.validUntil = source.xsdDateTime(VcdmVocab.VALID_UNTIL.uri());
//
//        entry.subject = source.fragment(
//                VcdmVocab.SUBJECT.uri(), 
//                BitstringStatusList.class, 
//                BitstringStatusList::of);
        
        return entry;
    }

    
    public String purpose() {
        return purpose;
    }

    public long index() {
        return index;
    }

    public URI credential() {
        return credential;
    }

    public int indexBitLength() {
        return indexBitLength;
    }

    public BitstringStatusListMessages messages() {
        return messages;
    }
    
    public Collection<URI> references() {
        return references;
    }

    @Override
    public URI id() {
        return id;
    }

    @Override
    public Collection<String> type() {
        return type;
    }

}
