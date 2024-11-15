package com.apicatalog.vc;

import com.apicatalog.linkedtree.orm.Adapter;
import com.apicatalog.linkedtree.orm.Compaction;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Mapper;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.multibase.MultibaseAdapter;
import com.apicatalog.multicodec.key.MulticodecKey;
import com.apicatalog.multikey.Multikey;

@Fragment
@Term("Multikey")
@Vocab("https://w3id.org/security#")
@Context(value = "https://w3id.org/security/multikey/v1", override = true)
public interface TestMultikey extends Multikey {

    @Term("publicKeyMultibase")
    @Adapter(MultibaseAdapter.class)
    @Mapper(TestMulticodecKeyMapper.class)
    @Compaction(order = 40)
    @Override
    MulticodecKey publicKey();

    @Term("secretKeyMultibase")
    @Adapter(MultibaseAdapter.class)
    @Mapper(TestMulticodecKeyMapper.class)
    @Compaction(order = 50)
    @Override
    MulticodecKey privateKey();
}
