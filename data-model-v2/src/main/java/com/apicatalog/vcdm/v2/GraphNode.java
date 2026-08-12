package com.apicatalog.vcdm.v2;

import java.util.Collection;

public record GraphNode(
        String id,
        Collection<String> types
        ) {

}
