package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.google.common.base.Optional;

import java.util.Map;

public class TestOptional extends BaseTest
{
    private final ObjectMapper MAPPER = mapperWithModule();

    @JsonAutoDetect(fieldVisibility=Visibility.ANY)
    public static final class OptionalData{
        private Optional<String> myString;
    }
    
    @JsonAutoDetect(fieldVisibility=Visibility.ANY)
    public static final class OptionalGenericData<T>{
        private Optional<T> myData;
    }

    @JsonAutoDetect(fieldVisibility=Visibility.ANY)
    public static final class OptionalRequiredData{
        @JsonProperty(required = true)
        private Optional<String> myRequiredString;
    }

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
    public static class Unit
    {
//        @JsonIdentityReference(alwaysAsId=true)
        public Optional<Unit> baseUnit;
        
        public Unit() { }
        public Unit(Optional<Unit> u) { baseUnit = u; }
        
        public void link(Unit u) {
            baseUnit = Optional.of(u);
        }
    }
    
    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    public void testDeserAbsent() throws Exception {
        Optional<?> value = MAPPER.readValue("null", new TypeReference<Optional<String>>() {});
        assertFalse(value.isPresent());
    }
    
    public void testDeserSimpleString() throws Exception{
        Optional<?> value = MAPPER.readValue("\"simpleString\"", new TypeReference<Optional<String>>() {});
        assertTrue(value.isPresent());
        assertEquals("simpleString", value.get());
    }
    
    public void testDeserInsideObject() throws Exception {
        OptionalData data = MAPPER.readValue("{\"myString\":\"simpleString\"}", OptionalData.class);
        assertTrue(data.myString.isPresent());
        assertEquals("simpleString", data.myString.get());
    }
    
    public void testDeserComplexObject() throws Exception {
        TypeReference<Optional<OptionalData>> type = new TypeReference<Optional<OptionalData>>() {};
        Optional<OptionalData> data = MAPPER.readValue("{\"myString\":\"simpleString\"}", type);
        assertTrue(data.isPresent());
        assertTrue(data.get().myString.isPresent());
        assertEquals("simpleString", data.get().myString.get());
    }
    
    public void testDeserGeneric() throws Exception {
        TypeReference<Optional<OptionalGenericData<String>>> type = new TypeReference<Optional<OptionalGenericData<String>>>() {};
        Optional<OptionalGenericData<String>> data = MAPPER.readValue("{\"myData\":\"simpleString\"}", type);
        assertTrue(data.isPresent());
        assertTrue(data.get().myData.isPresent());
        assertEquals("simpleString", data.get().myData.get());
    }
    
    public void testSerAbsent() throws Exception {
        String value = MAPPER.writeValueAsString(Optional.absent());
        assertEquals("null", value);
    }
    
    public void testSerSimpleString() throws Exception {
        String value = MAPPER.writeValueAsString(Optional.of("simpleString"));
        assertEquals("\"simpleString\"", value);
    }
    
    public void testSerInsideObject() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = Optional.of("simpleString");
        String value = MAPPER.writeValueAsString(data);
        assertEquals("{\"myString\":\"simpleString\"}", value);
    }
    
    public void testSerComplexObject() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = Optional.of("simpleString");
        String value = MAPPER.writeValueAsString(Optional.of(data));
        assertEquals("{\"myString\":\"simpleString\"}", value);
    }
    
    public void testSerGeneric() throws Exception {
        OptionalGenericData<String> data = new OptionalGenericData<String>();
        data.myData = Optional.of("simpleString");
        String value = MAPPER.writeValueAsString(Optional.of(data));
        assertEquals("{\"myData\":\"simpleString\"}", value);
    }

    public void testSerNonNull() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = Optional.absent();
        String value = mapperWithModule().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(data);
        assertEquals("{}", value);
    }
    
    public void testSerOptNull() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = null;
        String value = mapperWithModule().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(data);
        assertEquals("{}", value);
    }
    
    public void testWithTypingEnabled() throws Exception
    {
		final ObjectMapper objectMapper = mapperWithModule();
		// ENABLE TYPING
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

		final OptionalData myData = new OptionalData();
		myData.myString = Optional.fromNullable("abc");
		
		final String json = objectMapper.writeValueAsString(myData);
		
		final OptionalData deserializedMyData = objectMapper.readValue(json, OptionalData.class);
		assertEquals(myData.myString, deserializedMyData.myString);
    }

    // for [Issue#17]
    public void testObjectId() throws Exception
    {
        final Unit input = new Unit();
        input.link(input);
        String json = MAPPER.writeValueAsString(input);
        Unit result = MAPPER.readValue(json,  Unit.class);
        assertNotNull(result);
        assertNotNull(result.baseUnit);
        assertTrue(result.baseUnit.isPresent());
        Unit base = result.baseUnit.get();
        assertSame(result, base);
    }

    public void testSchemaGeneric() throws Exception {
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        MAPPER.acceptJsonFormatVisitor(OptionalData.class, visitor);
        JsonSchema jsonSchema = visitor.finalSchema();
        assertNotNull(jsonSchema);
        assertTrue(jsonSchema.isObjectSchema());
        Map<String, JsonSchema> properties = jsonSchema.asObjectSchema().getProperties();
        assertEquals(properties.size(), 1);
        Map.Entry<String, JsonSchema> property = properties.entrySet().iterator().next();
        assertEquals(property.getKey(), "myString");
        assertTrue(property.getValue().isStringSchema());
    }

    public void testSchemaRequired() throws Exception {
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        MAPPER.acceptJsonFormatVisitor(OptionalRequiredData.class, visitor);
        JsonSchema jsonSchema = visitor.finalSchema();
        assertNotNull(jsonSchema);
        assertTrue(jsonSchema.isObjectSchema());
        Map<String, JsonSchema> properties = jsonSchema.asObjectSchema().getProperties();
        assertEquals(properties.size(), 1);
        Map.Entry<String, JsonSchema> property = properties.entrySet().iterator().next();
        assertEquals(property.getKey(), "myRequiredString");
        assertTrue(property.getValue().getRequired() == null || !property.getValue().getRequired());
    }
}
