package com.apicatalog.vcdm.v2;

import java.util.Collection;

public record Node(
        String id,
        Collection<String> type,
        Collection<String[]> statements) {

}
