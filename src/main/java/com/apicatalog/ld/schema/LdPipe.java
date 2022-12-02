package com.apicatalog.ld.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.schema.adapter.LdValueAdapter;

public class LdPipe<A, B> implements LdValueAdapter<A, B> {

    private final Collection<LdValueAdapter<Object, Object>> adapters;

    protected LdPipe(LdValueAdapter<Object, Object> adapter) {
        this.adapters = new ArrayList<>(10);
        this.adapters.add(adapter);
    }

    @Override
    public B read(A value) throws DocumentError {

        Object result = value;

        for (final LdValueAdapter<Object, Object> adapter : adapters) {
            result = adapter.read(result);
        }

        return (B) result;
    }

    @Override
    public A write(B value) throws DocumentError {

        final List<LdValueAdapter<Object, Object>> reversed = new ArrayList<>(adapters);

        Collections.reverse(reversed);

        Object result = value;

        for (final LdValueAdapter<Object, Object> adapter : reversed) {
            result = adapter.write(result);
        }

        return (A) result;
    }

    @SuppressWarnings("unchecked")
    public <C> LdPipe<A, C> map(LdValueAdapter<B, C> adapter) {
        adapters.add((LdValueAdapter<Object, Object>) adapter);
        return (LdPipe<A, C>) this;
    }

    @SuppressWarnings("unchecked")
    public static <A, B> LdPipe<A, B> create(LdValueAdapter<A, B> adapter) {
        if (adapter instanceof LdPipe) {
            return (LdPipe<A, B>)adapter;
        }
        return new LdPipe<A, B>((LdValueAdapter<Object, Object>) adapter);
    }
}
