package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.ForwardingTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unit test to verify serialization and deserialization of {@link Table}.
 *
 * @author Michael Hixson
 */
public final class TestTables extends ModuleTestBase
{
  private final ObjectMapper MAPPER = mapperWithModule();

  /**
   * Tests that we can convert a {@link Table} instance (the interface, not one
   * of the concrete implementations provided by Guava) to and from JSON.
   *
   * <p>Serialization must preserve cell ordering.
   */
  public void testDefaultTables() throws Exception
  {
    Table<Integer, Double, String> original = Tables.newCustomTable(
        new LinkedHashMap<Integer, Map<Double, String>>(),
        new Supplier<Map<Double, String>>()
        {
          @Override
          public Map<Double, String> get()
          {
            return new LinkedHashMap<Double, String>();
          }
        });
    original.put(3, 3.0, "three");
    original.put(2, 2.0, "two");
    original.put(1, 1.0, "one");
    String serialized = MAPPER.writeValueAsString(original);
    assertEquals(
        "{\"3\":{\"3.0\":\"three\"},"
            + "\"2\":{\"2.0\":\"two\"},"
            + "\"1\":{\"1.0\":\"one\"}}",
        serialized);
    Table<Integer, Double, String> deserialized = MAPPER.readValue(
        serialized,
        new TypeReference<Table<Integer, Double, String>>() {});
    assertTrue(
        "Expected = " + original.cellSet()
            + ", actual = " + deserialized.cellSet(),
        Iterables.elementsEqual(
            original.cellSet(),
            deserialized.cellSet()));
  }

  /**
   * Tests that we can convert a {@link ImmutableTable} instance to and from
   * JSON.
   *
   * <p>Serialization must preserve cell ordering.
   */
  public void testImmutableTables() throws Exception
  {
    ImmutableTable<Integer, Double, String> original = ImmutableTable
        .<Integer, Double, String>builder()
        .put(3, 3.0, "three")
        .put(2, 2.0, "two")
        .put(1, 1.0, "one")
        .build();
    String serialized = MAPPER.writeValueAsString(original);
    assertEquals(
        "{\"3\":{\"3.0\":\"three\"},"
            + "\"2\":{\"2.0\":\"two\"},"
            + "\"1\":{\"1.0\":\"one\"}}",
        serialized);
    ImmutableTable<Integer, Double, String> deserialized = MAPPER.readValue(
        serialized,
        new TypeReference<ImmutableTable<Integer, Double, String>>() {});
    assertTrue(
        "Expected = " + original.cellSet()
            + ", actual = " + deserialized.cellSet(),
        Iterables.elementsEqual(
            original.cellSet(),
            deserialized.cellSet()));
  }

  /**
   * Tests that we can convert a {@link HashBasedTable} instance to and from
   * JSON.
   *
   * <p>Serialization might not preserve cell ordering.  We can't test for the
   * exact serialized form of the table because {@link HashBasedTable} makes no
   * guarantees on the iteration order of its cells.  So unlike the other tests
   * involving {@link Table}, we don't make assertions about the serialized JSON
   * string or the ordering of cells in the tables.
   */
  public void testHashBasedTables() throws Exception
  {
    HashBasedTable<Integer, Double, String> original = HashBasedTable.create();
    original.put(3, 3.0, "three");
    original.put(2, 2.0, "two");
    original.put(1, 1.0, "one");
    String serialized = MAPPER.writeValueAsString(original);
    HashBasedTable<Integer, Double, String> deserialized = MAPPER.readValue(
        serialized,
        new TypeReference<HashBasedTable<Integer, Double, String>>() {});
    assertEquals(original, deserialized);
  }

  /**
   * Tests that we can convert a {@link ArrayTable} instance to and from JSON.
   *
   * <p>Serialization must preserve cell ordering.
   *
   * <p>We intentionally leave at least one row and column blank to ensure that
   * the row and column sets are preserved in the JSON, which would not be the
   * case if we omitted {@code null} values from the JSON.
   */
  public void testArrayTables() throws Exception
  {
    ArrayTable<Integer, Double, String> original = ArrayTable.create(
        ImmutableSet.of(3, 2, 1),
        ImmutableSet.of(3.0, 2.0, 1.0));
    original.put(3, 3.0, "three");
    original.put(2, 2.0, "two");
    // Intentionally omit values for r=1, c=1.0
    String serialized = MAPPER.writeValueAsString(original);
    assertEquals(
        "{\"3\":{\"3.0\":\"three\",\"2.0\":null,\"1.0\":null},"
            + "\"2\":{\"3.0\":null,\"2.0\":\"two\",\"1.0\":null},"
            + "\"1\":{\"3.0\":null,\"2.0\":null,\"1.0\":null}}",
        serialized);
    ArrayTable<Integer, Double, String> deserialized = MAPPER.readValue(
        serialized,
        new TypeReference<ArrayTable<Integer, Double, String>>() {});
    assertTrue(
        "Expected = " + original.cellSet()
            + ", actual = " + deserialized.cellSet(),
        Iterables.elementsEqual(
            original.cellSet(),
            deserialized.cellSet()));
  }

  /**
   * Tests that tables lacking generic type information will interpret rows and
   * column keys as strings during deserialization.  A wrong implementation of
   * the table deserializers might throw a null pointer exception due to missing
   * generic type parameters.  This test ensures that we treat missing type
   * parameters as "unknown" rather than null.
   *
   * <p>Serialization must preserve cell ordering.
   */
  // This unit test is specifically about how tables serialize and deserialize
  // when using raw types.  Of course we have unchecked casts and raw types.
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testRawTypedTables() throws Exception
  {
    Table original = Tables.newCustomTable(
        new LinkedHashMap<Integer, Map<Double, String>>(),
        new Supplier<Map<Double, String>>()
        {
          @Override
          public Map<Double, String> get()
          {
            return new LinkedHashMap<Double, String>();
          }
        });
    original.put(3, 3.0, "three");
    original.put(2, 2.0, "two");
    original.put(1, 1.0, "one");
    String serialized = MAPPER.writeValueAsString(original);
    assertEquals(
        "{\"3\":{\"3.0\":\"three\"},"
            + "\"2\":{\"2.0\":\"two\"},"
            + "\"1\":{\"1.0\":\"one\"}}",
        serialized);
    Table deserialized = MAPPER.readValue(
        serialized,
        new TypeReference<Table>() {});
    assertFalse(
        "Since we were using raw types, deserialization should have converted "
            + "all the Integer row keys and Double column keys to strings",
        original.equals(deserialized));
    ImmutableList<Table.Cell<String, String, String>> expected =
        ImmutableList.of(
            Tables.immutableCell("3", "3.0", "three"),
            Tables.immutableCell("2", "2.0", "two"),
            Tables.immutableCell("1", "1.0", "one"));
    assertTrue(
        "Expected = " + expected + ", actual = " + deserialized.cellSet(),
        Iterables.elementsEqual(expected, deserialized.cellSet()));
  }

  /**
   * Tests that deserialization into a {@link Table} implementation that is not
   * one of the supported types fails.
   */
  public void testUnsupportedTables() throws Exception
  {
    try
    {
      MAPPER.readValue(
          "{}",
          new TypeReference<ForwardingTable<String, String, String>>() {});
      fail("Deserialization of a non-supported table type should fail");
    }
    catch (JsonMappingException ignored) {}
  }
}
