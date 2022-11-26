package com.apicatalog.ld.signature.adapter;

import java.net.URI;
import java.util.Optional;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.PropertyName;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.key.MultibasePublicKey;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.multicodec.Multicodec.Type;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class MultibaseKeyAdapter implements MethodAdapter {

    protected final URI type;
    protected final Codec codec;
    protected final PropertyName keyProperty;
    
    public MultibaseKeyAdapter(URI type, Codec codec, PropertyName keyProperty) {
        this.type = type;
        this.codec = codec;
        this.keyProperty = keyProperty;
    }
    
    @Override
    public URI type() {
        return type;
    }

    @Override
    public VerificationMethod deserialize(JsonObject object) throws DocumentError {
        
        try {
            URI id = JsonLdReader.getId(object).orElse(null);
                
            byte[] publicKey = publicKeyFrom(object).orElse(null);
            System.out.println(">>> " + id);
            System.out.println(">>> " + keyProperty.id());
            System.out.println(">>> " + publicKey);
            // TODO Auto-generated method stub
            return new MultibasePublicKey(
                            id, 
                            type.toString(), 
                            publicKey, 
                            codec
                        );
            
        } catch (InvalidJsonLdValue e) {
            //TODO
            e.printStackTrace();
            throw new DocumentError(ErrorType.Invalid, "VerificationKey", e);
        }
    }

    @Override
    public JsonObject serialize(VerificationMethod proof) throws DocumentError {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    final Optional<byte[]> publicKeyFrom(JsonObject json) throws DocumentError {

        // public key
        if (JsonLdReader.hasPredicate(json, keyProperty.id())) {
            
            byte[] publicKey = getKey(json, keyProperty, codec); 
            
            // verify verification key length
//            if (publicKey != null
//                    && publicKey.length != 32
//                    && publicKey.length != 57
//                    && publicKey.length != 114
//                    ) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValueLenght");
//            }

            return Optional.ofNullable(publicKey);
        }

        return Optional.empty();
    }

    static byte[] getKey(JsonObject json, PropertyName property, Codec expected) throws DocumentError {

        JsonValue key = JsonLdReader
                            .getObjects(json, property.id())
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new DocumentError(ErrorType.Missing, Keywords.TYPE));

        if (JsonUtils.isArray(key)) {
            key = key.asJsonArray().get(0);
        }

        if (!ValueObject.isValueObject(key)) {
            throw new DocumentError(ErrorType.Invalid, property.name());
        }

//FIXME        if (!JsonLdReader.isTypeOf(PUBLIC_KEY_TYPE_VALUE, key.asJsonObject())) {
//            throw new DocumentError(ErrorType.Invalid, property, Keywords.TYPE);
//        }

        final String keyMultibase = ValueObject
                        .getValue(key)
                        .filter(JsonUtils::isString)
                        .map(JsonString.class::cast)
                        .map(JsonString::getString)
                        .orElseThrow(() -> new DocumentError(ErrorType.Invalid, property.name()));
        // decode private key
        final byte[] encodedKey = Multibase.decode(keyMultibase);

        final Codec codec = Multicodec
                    .codec(Type.Key, encodedKey)
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid, property.name()));

        if (expected != codec) {
            throw new DocumentError(ErrorType.Invalid, property.name());
        }

        return Multicodec.decode(codec, encodedKey);
    }

}
