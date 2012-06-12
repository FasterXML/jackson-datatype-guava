package com.fasterxml.jackson.module.guava;


import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.type.TypeReference;
import com.google.common.base.Optional;

public class TestOptional extends BaseTest {
    public void testDeserAbsent() throws Exception {
        Optional<?> value = mapperWithModule().readValue("null", new TypeReference<Optional<String>>() {});
        assertFalse(value.isPresent());
    }
    
    public void testDeserSimpleString() throws Exception{
        Optional<?> value = mapperWithModule().readValue("\"simpleString\"", new TypeReference<Optional<String>>() {});
        assertTrue(value.isPresent());
        assertEquals("simpleString", value.get());
    }
    
    public void testDeserInsideObject() throws Exception {
        OptionalData data = mapperWithModule().readValue("{\"myString\":\"simpleString\"}", OptionalData.class);
        assertTrue(data.myString.isPresent());
        assertEquals("simpleString", data.myString.get());
    }
    
    public void testDeserComplexObject() throws Exception {
        TypeReference<Optional<OptionalData>> type = new TypeReference<Optional<OptionalData>>() {};
        Optional<OptionalData> data = mapperWithModule().readValue("{\"myString\":\"simpleString\"}", type);
        assertTrue(data.isPresent());
        assertTrue(data.get().myString.isPresent());
        assertEquals("simpleString", data.get().myString.get());
    }
    
    public void testDeserGeneric() throws Exception {
        TypeReference<Optional<OptionalGenericData<String>>> type = new TypeReference<Optional<OptionalGenericData<String>>>() {};
        Optional<OptionalGenericData<String>> data = mapperWithModule().readValue("{\"myData\":\"simpleString\"}", type);
        assertTrue(data.isPresent());
        assertTrue(data.get().myData.isPresent());
        assertEquals("simpleString", data.get().myData.get());
    }
    
    public void testSerAbsent() throws Exception {
        String value = mapperWithModule().writeValueAsString(Optional.absent());
        assertEquals("null", value);
    }
    
    public void testSerSimpleString() throws Exception {
        String value = mapperWithModule().writeValueAsString(Optional.of("simpleString"));
        assertEquals("\"simpleString\"", value);
    }
    
    public void testSerInsideObject() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = Optional.of("simpleString");
        String value = mapperWithModule().writeValueAsString(data);
        assertEquals("{\"myString\":\"simpleString\"}", value);
    }
    
    public void testSerComplexObject() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = Optional.of("simpleString");
        String value = mapperWithModule().writeValueAsString(Optional.of(data));
        assertEquals("{\"myString\":\"simpleString\"}", value);
    }
    
    public void testSerGeneric() throws Exception {
        OptionalGenericData<String> data = new OptionalGenericData<String>();
        data.myData = Optional.of("simpleString");
        String value = mapperWithModule().writeValueAsString(Optional.of(data));
        assertEquals("{\"myData\":\"simpleString\"}", value);
    }
    
    @JsonAutoDetect(fieldVisibility=Visibility.ANY)
    public static final class OptionalData{
        private Optional<String> myString;
    }
    
    @JsonAutoDetect(fieldVisibility=Visibility.ANY)
    public static final class OptionalGenericData<T>{
        private Optional<T> myData;
    }
}
