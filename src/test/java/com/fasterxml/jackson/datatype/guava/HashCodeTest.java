package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashCode;

public class HashCodeTest extends ModuleTestBase
{
    private final ObjectMapper MAPPER = mapperWithModule();

    public void testSerialization() throws Exception
    {
        HashCode input = HashCode.fromString("cafebabe12345678");
        String json = MAPPER.writeValueAsString(input);
        assertEquals("\"cafebabe12345678\"", json);
    }

    public void testDeserialization() throws Exception
    {
        // success:
        HashCode result = MAPPER.readValue(quote("12345678cafebabe"), HashCode.class);
        assertEquals("12345678cafebabe", result.toString());

        // and ... error (note: numbers, booleans may all be fine)
        try {
            result = MAPPER.readValue("[ ]", HashCode.class);
            fail("Should not deserialize from boolean: got "+result);
        } catch (JsonProcessingException e) {
            verifyException(e, "Can not deserialize");
        }
    }
}
