package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.google.common.net.HostAndPort;

public class HostAndPortDeserializer extends StdDeserializer<HostAndPort>
{
    private static final long serialVersionUID = 1L;

    public final static HostAndPortDeserializer std = new HostAndPortDeserializer();
    
    public HostAndPortDeserializer() { super(HostAndPort.class); }

    @Override
    public HostAndPort deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) { // old style
            JsonNode root = jp.readValueAsTree();
            String host = root.path("hostText").asText();
            JsonNode n = root.get("port");
            if (n == null) {
                return HostAndPort.fromString(host);
            }
            return HostAndPort.fromParts(host, n.asInt());
        }
        if (t == JsonToken.VALUE_STRING) {
            return HostAndPort.fromString(jp.getText().trim());
        }
        // could also support arrays?
        throw ctxt.wrongTokenException(jp, JsonToken.VALUE_STRING, "(or JSON Object)");
    }

}
