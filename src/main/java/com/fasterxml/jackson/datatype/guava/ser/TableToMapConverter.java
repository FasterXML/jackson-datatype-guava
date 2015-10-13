package com.fasterxml.jackson.datatype.guava.ser;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.google.common.collect.Table;

import java.util.Map;

/**
 * Converts {@link Table} instances into {@link Map} instances during JSON
 * serialization, using {@link Table#rowMap()}.
 *
 * @param <R> the type of the table row keys
 * @param <C> the type of the table column keys
 * @param <V> the type of the mapped values
 */
public final class TableToMapConverter<R, C, V>
    implements Converter<Table<R, C, V>, Map<R, Map<C, V>>>
{
  private final JavaType tableType;

  /**
   * Constructs a new converter with the provided table type.
   *
   * @param tableType the type of the table being serialized
   */
  public TableToMapConverter(JavaType tableType)
  {
    if (tableType == null)
    {
      throw new NullPointerException("tableType must not be null");
    }
    this.tableType = tableType;
  }

  @Override
  public Map<R, Map<C, V>> convert(Table<R, C, V> table)
  {
    return table.rowMap();
  }

  @Override
  public JavaType getInputType(TypeFactory typeFactory)
  {
    return tableType;
  }

  @Override
  public JavaType getOutputType(TypeFactory typeFactory)
  {
    return typeFactory.constructRawMapType(Map.class);
  }
}
