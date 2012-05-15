package com.fasterxml.jackson.datatype.guava.deser;


import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.collect.HashMultiset;

public class HashMultisetDeserializer
 extends GuavaMultisetDeserializer<HashMultiset<Object>>
{
    public HashMultisetDeserializer(CollectionType type,
            TypeDeserializer typeDeser, JsonDeserializer<?> deser)
    {
        super(type, typeDeser, deser);
    }

    @Override
    public HashMultisetDeserializer withResolved(TypeDeserializer typeDeser,
            JsonDeserializer<?> valueDeser) {
        return new HashMultisetDeserializer(_containerType,
                typeDeser, valueDeser);
    }
    
    @Override
    protected HashMultiset<Object> createMultiset() {
        return HashMultiset.<Object> create();
    }
}
