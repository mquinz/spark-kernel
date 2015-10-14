package com.ibm.spark.magic.builtin

import java.text.SimpleDateFormat
import com.datastax.driver.core.DataType.Name._
import scala.collection.JavaConverters._
import com.datastax.driver.core.{DataType, UDTValue, GettableByNameData}
import collection.JavaConversions._

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2015 DataStax
 *
 */
object CqlHelper {
  val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

  implicit class EscapableString(s: String) {
    def escape = xml.Utility.escape(s)
  }

  implicit class GettableAsString[RowOrUDT <: GettableByNameData](row: RowOrUDT) {
    def getAsString(colName: String, colType: DataType): String = {
      colType.getName match {
        case ASCII => row.getString(colName)
        case BIGINT => row.getLong(colName)
        case BLOB => row.getString(colName)
        case BOOLEAN => row.getBool(colName)
        case COUNTER => row.getLong(colName)
        case CUSTOM => row.getString(colName)
        case DECIMAL => row.getDecimal(colName)
        case DOUBLE => row.getDouble(colName)
        case FLOAT => row.getFloat(colName)
        case INET => row.getInet(colName)
        case INT => row.getInt(colName)
        case TEXT => row.getString(colName)
        case TIMESTAMP => df.format(row.getDate(colName))
        case TIMEUUID => row.getUUID(colName)
        case TUPLE => row.getTupleValue(colName)
        case UUID => row.getUUID(colName)
        case VARCHAR => row.getString(colName)
        case VARINT => row.getVarint(colName)
        case LIST => row.getList(colName, colType.getTypeArguments.get(0).asJavaClass)
        case MAP => row.getMap(colName, colType.getTypeArguments.get(0).asJavaClass, colType.getTypeArguments.get(1).asJavaClass)
        case SET => row.getSet(colName, colType.getTypeArguments.get(0).asJavaClass)
        case UDT =>  val v = row.getUDTValue(colName); "{" + v.getType.getFieldNames.map(n => n + ":" + v.getAsString(n, v.getType.getFieldType(n))).mkString(", ") + "}"
      }
    }.toString
  }

  implicit class RowHelper(row: com.datastax.driver.core.Row) {
    def getStringForCol(colDef: com.datastax.driver.core.ColumnDefinitions.Definition): String = {
      val colName = colDef.getName

      if (row.isNull(colName))
        "<null>"
      else
          row.getAsString(colName, colDef.getType)
    }.escape
  }
}
