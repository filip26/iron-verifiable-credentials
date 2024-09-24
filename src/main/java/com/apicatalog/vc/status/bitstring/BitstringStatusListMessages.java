package com.apicatalog.vc.status.bitstring;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public record BitstringStatusListMessages(
        Map<Integer, String> messages) {

    public BitstringStatusListMessages {
        Objects.requireNonNull(messages);
    }

    public Collection<Integer> code() {
        return messages.keySet();
    }

    public String message(int code) {
        return messages.get(code);
    }

    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }
}
