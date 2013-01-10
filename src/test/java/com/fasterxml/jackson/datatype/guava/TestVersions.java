package com.fasterxml.jackson.datatype.guava;

import java.io.*;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.guava.ModuleVersion;
import com.fasterxml.jackson.datatype.guava.PackageVersion;

public class TestVersions extends BaseTest
{
    private final static String GROUP_ID = "com.fasterxml.jackson.datatype";
    private final static String ARTIFACT_ID = "jackson-datatype-guava";

    public void testMapperVersions() throws IOException
    {
        GuavaModule module = new GuavaModule();
        assertVersion(module);
    }

    public void testPackageVersion()
    {
        assertEquals(PackageVersion.VERSION, ModuleVersion.instance.version());
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */

    private void assertVersion(Versioned vers)
    {
        final Version v = vers.version();
        assertFalse("Should find version information (got "+v+")", v.isUknownVersion());
        assertEquals(PackageVersion.VERSION, v);
    }
}

