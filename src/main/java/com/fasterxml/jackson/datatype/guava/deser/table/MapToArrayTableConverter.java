package com.fasterxml.jackson.datatype.guava.deser.table;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Converts {@link Map} instances into {@link ArrayTable} instances during JSON
 * deserialization.
 *
 * @param <R> the type of the table row keys
 * @param <C> the type of the table column keys
 * @param <V> the type of the mapped values
 *
 * @author Michael Hixson
 */
public final class MapToArrayTableConverter<R, C, V>
    extends MapToTableConverter<R, C, V>
{
  /**
   * Constructs a new converter with the provided table type.
   *
   * @param tableType the type of the table being deserialized
   */
  public MapToArrayTableConverter(JavaType tableType)
  {
    super(tableType);
  }

  @Override
  public Table<R, C, V> convert(Map<R, Map<C, V>> map)
  {
    Set<R> rowKeys = map.keySet();
    Set<C> columnKeys = new LinkedHashSet<C>();
    for (Map<C, V> columnMap : map.values())
    {
      columnKeys.addAll(columnMap.keySet());
    }
    Table<R, C, V> table = ArrayTable.create(rowKeys, columnKeys);
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
