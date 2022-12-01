package com.apicatalog.ld.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LdPipe<A, B> implements LdValueAdapter<A, B> {

    private final Collection<LdValueAdapter<Object, Object>> adapters;
    
    @SuppressWarnings("unchecked")
    public LdPipe(LdValueAdapter<A, B> adapter) {
        this.adapters = new ArrayList<>(5);
        this.adapters.add((LdValueAdapter<Object, Object>) adapter);
    }

    @Override
    public B read(A value) {
        
        Object result = value;

        for (final LdValueAdapter<Object, Object> adapter : adapters) {
            result = adapter.read(result);
        }
        
        return (B)result;
    }

    @Override
    public A write(B value) {

        final List<LdValueAdapter<Object, Object>> reversed = new ArrayList<>(adapters);
        
        Collections.reverse(reversed);
        
        Object result = value;

        for (final LdValueAdapter<Object, Object> adapter :  reversed) {
            result = adapter.write(result);
        }
        
        return (A)result;
    }
    
    @SuppressWarnings("unchecked")
    public <C> LdPipe<A, C> map(LdValueAdapter<B, C> adapter) {
        adapters.add((LdValueAdapter<Object, Object>) adapter);
        return (LdPipe<A, C>)this;
    }

    public static <A, B, C> LdPipe<A, C> map(LdValueAdapter<A, B> adapter1,  LdValueAdapter<B, C> adapter2) {
        
        if (adapter1 instanceof LdPipe) {
            return ((LdPipe<A, B>)adapter1).map(adapter2);            
        }
        
        return new LdPipe<A, B>(adapter1).map(adapter2);
    }
}
