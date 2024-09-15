package com.apicatalog.vcdm.v11;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vcdm.VcdmPresentation;
import com.apicatalog.vcdm.VcdmVersion;

public class Vcdm11Presentation extends VcdmPresentation implements Presentation {

    public static Presentation of(LinkedFragment source) throws AdapterError {
        return setup(new Vcdm11Presentation(), source);
    }

    @Override
    public VcdmVersion version() {
        return VcdmVersion.V11;
    }
}
