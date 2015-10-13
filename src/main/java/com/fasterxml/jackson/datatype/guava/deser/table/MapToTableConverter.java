package com.fasterxml.jackson.datatype.guava.deser.table;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.google.common.collect.Table;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An abstract base class for converting {@link Map} instances into
 * {@link Table} instances during JSON deserialization.  This class assumes that
 * the maps mirror the structure of {@link Table#rowMap()}.  The maps are
 * interpreted as {@link LinkedHashMap} to preserve cell ordering, in case the
 * table implementation also preserves cell ordering during insertion.
 *
 * @param <R> the type of the table row keys
 * @param <C> the type of the table column keys
 * @param <V> the type of the mapped values
 *
 * @author Michael Hixson
 */
abstract class MapToTableConverter<R, C, V>
    implements Converter<Map<R, Map<C, V>>, Table<R, C, V>>
{
  private final JavaType tableType;

  /**
   * Constructs a new converter with the provided table type.
   *
   * @param tableType the type of the table being deserialized
   */
  MapToTableConverter(JavaType tableType)
  {
    if (tableType == null)
    {
      throw new NullPointerException("tableType must not be null");
    }
    this.tableType = tableType;
  }

  @Override
  public JavaType getInputType(TypeFactory typeFactory)
  {
    JavaType rowKeyType = tableType.containedTypeOrUnknown(0);
    JavaType columnKeyType = tableType.containedTypeOrUnknown(1);
    JavaType valueType = tableType.containedTypeOrUnknown(2);
    return typeFactory.constructMapType(
        LinkedHashMap.class,
        rowKeyType,
        typeFactory.constructMapType(
            LinkedHashMap.class,
            columnKeyType,
            valueType));
  }

  @Override
  public JavaType getOutputType(TypeFactory typeFactory)
  {
    return tableType;
  }
}
