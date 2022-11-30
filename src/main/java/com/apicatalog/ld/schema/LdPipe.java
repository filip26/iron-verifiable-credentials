package com.apicatalog.ld.schema;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.json.JsonValue;

public class LdPipe<A, B> implements LdValueAdapter<A, B> {

    private final Collection<LdValueAdapter<?, ?>> adapters;
    
    public LdPipe(LdValueAdapter<A, B> adapter) {
        this.adapters = new ArrayList<>(5);
        this.adapters.add(adapter);
    }

    @Override
    public B read(A value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public A write(B value) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public <C> LdPipe<A, C> map(LdValueAdapter<B, C> adapter) {
        adapters.add(adapter);
        return (LdPipe<A, C>)this;
    }

    public static <A, B, C> LdPipe<A, C> map(LdValueAdapter<A, B> adapter1,  LdValueAdapter<B, C> adapter2) {
        
        if (adapter1 instanceof LdPipe) {
            return ((LdPipe<A, B>)adapter1).map(adapter2);            
        }
        
        return new LdPipe<A, B>(adapter1).map(adapter2);
    }    
}
