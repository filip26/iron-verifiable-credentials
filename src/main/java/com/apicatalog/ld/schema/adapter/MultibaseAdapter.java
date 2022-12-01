package com.apicatalog.ld.schema.adapter;

import com.apicatalog.ld.schema.LdValueAdapter;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.multicodec.Multicodec.Type;

public class MultibaseAdapter implements LdValueAdapter<String, byte[]> {

    protected final Algorithm algorithm;
    protected final Type multicodec;
    
    public MultibaseAdapter(Algorithm algorithm) {
        this(algorithm, null);
    }

    public MultibaseAdapter(Algorithm algorithm, Type multicodec) {
        this.algorithm = algorithm;
        this.multicodec = multicodec;
    }

    @Override
    public byte[] read(String value) {
        
        final byte[] debased = Multibase.decode(value); // ;)
        
        if (multicodec == null) {
            return debased;
        }

        System.out.println(">>> " + Multicodec.codec(Type.Key, debased));
        
        final Codec codec = Multicodec
                                    .codec(multicodec, debased)
                                    .orElseThrow(() -> new IllegalArgumentException());


        return Multicodec.decode(codec, debased); 
    }

    @Override
    public String write(byte[] value) {
        return Multibase.encode(algorithm, value);
    }
//    
//    public static void main(String[] args) {
//        
//        byte[] x = Multibase.decode("zF9RuvG5hsw3QBW5ZzwdiHXZTESQ5qQDoPcLDbwojW818");
//        x = Multicodec.encode(Codec.Ed25519PublicKey,x);
//        
//        System.out.println(Multibase.encode(Algorithm.Base58Btc, x));
//        
//        
//        
//    }
//
}
