package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.Map;
import java.util.SequencedCollection;

public record Graph(
        String id,
        Map<String, Node> nodes) {

    public record Node(
            String id,
            SequencedCollection<String> type,
            Collection<String[]> statements) {
        
    }

}
