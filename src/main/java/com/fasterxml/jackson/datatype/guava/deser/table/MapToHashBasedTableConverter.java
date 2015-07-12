package com.fasterxml.jackson.datatype.guava.deser.table;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Map;

/**
 * Converts {@link Map} instances into {@link HashBasedTable} instances during
 * JSON deserialization.
 *
 * @param <R> the type of the table row keys
 * @param <C> the type of the table column keys
 * @param <V> the type of the mapped values
 *
 * @author Michael Hixson
 */
public final class MapToHashBasedTableConverter<R, C, V>
    extends MapToTableConverter<R, C, V>
{
  /**
   * Constructs a new converter with the provided table type.
   *
   * @param tableType the type of the table being deserialized
   */
  public MapToHashBasedTableConverter(JavaType tableType)
  {
    super(tableType);
  }

  @Override
  public Table<R, C, V> convert(Map<R, Map<C, V>> map)
  {
    Table<R, C, V> table = HashBasedTable.create();
    for (Map.Entry<R, Map<C, V>> rowEntry : map.entrySet())
    {
      for (Map.Entry<C, V> columnEntry : rowEntry.getValue().entrySet())
      {
        R rowKey = rowEntry.getKey();
        C columnKey = columnEntry.getKey();
        V value = columnEntry.getValue();
        table.put(rowKey, columnKey, value);
      }
    }
    return table;
  }
}
