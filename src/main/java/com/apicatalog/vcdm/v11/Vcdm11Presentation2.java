package com.apicatalog.vcdm.v11;

import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vcdm.VcdmPresentation2;
import com.apicatalog.vcdm.VcdmVersion;

@Fragment
@Term("VerifiablePresentation")
@Vocab("https://www.w3.org/2018/credentials#")
public interface Vcdm11Presentation2 extends VcdmPresentation2, Presentation {

//    public static Presentation of(LinkedFragment source) throws NodeAdapterError {
//        return setup(new Vcdm11Presentation2(), source);
//    }

    @Override
    default VcdmVersion version() {
        return VcdmVersion.V11;
    }
}
