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

  implicit class RowHelper(row: com.datastax.driver.core.Row) {
    def getStringForCol(colDef: com.datastax.driver.core.ColumnDefinitions.Definition): String = {
      val colName = colDef.getName

      if (row.isNull(colName))
        "null"
      else
          row.getObject(colName).toString
    }.escape
  }
}
