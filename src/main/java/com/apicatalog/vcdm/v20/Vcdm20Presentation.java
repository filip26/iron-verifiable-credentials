package com.apicatalog.vcdm.v20;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vcdm.VcdmPresentation;
import com.apicatalog.vcdm.VcdmVersion;

public class Vcdm20Presentation extends VcdmPresentation implements Presentation {

    public static Presentation of(LinkedFragment source) throws NodeAdapterError {
        return setup(new Vcdm20Presentation(), source);
    }

    @Override
    public VcdmVersion version() {
        return VcdmVersion.V20;
    }
}
