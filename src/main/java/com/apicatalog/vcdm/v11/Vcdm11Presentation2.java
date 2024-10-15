package com.apicatalog.vcdm.v11;

import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vcdm.VcdmPresentation2;
import com.apicatalog.vcdm.VcdmVersion;

@Fragment
public interface Vcdm11Presentation2 extends VcdmPresentation2, Presentation {

//    public static Presentation of(LinkedFragment source) throws NodeAdapterError {
//        return setup(new Vcdm11Presentation2(), source);
//    }

    @Override
    default VcdmVersion version() {
        return VcdmVersion.V11;
    }
}
