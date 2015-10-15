/*
 * Copyright 2014 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.spark.magic.builtin

import java.io.PrintStream

import com.datastax.spark.connector.cql._
import com.google.common.base.Strings
import com.ibm.spark.kernel.protocol.v5.MIMEType
import com.ibm.spark.magic._
import com.ibm.spark.magic.dependencies.{IncludeOutputStream, IncludeSparkContext}
import com.ibm.spark.utils.ArgumentParsingSupport

class ShowSchema extends CellMagic with ArgumentParsingSupport
with IncludeOutputStream with IncludeSparkContext {

  // Lazy because the outputStream is not provided at construction
  private lazy val printStream = new PrintStream(outputStream)

  override def execute(code: String): CellMagicOutput = {
    def printHelpAndReturn: CellMagicOutput = {
      printHelp(printStream, """%%ShowSchema <keyspace>.<table>""")
      CellMagicOutput()
    }

    CellMagicOutput(MIMEType.TextHtml -> getSchema(code))
  }

  def getSchema(ksTable: String) =  {

    def col2td (c: ColumnDef) = "<tr><td>" + c.columnName + "</td><td>" + c.columnType + "</td><td>" +
      (c.columnRole match { case RegularColumn => ""
      case PartitionKeyColumn => "partition key"
      case ClusteringColumn(x) => "cluster key " + x.toString
      case StaticColumn => "static"}) + "</td></tr>"

    def table2tbl (t:TableDef) = "<b>" +
      t.keyspaceName + "." + t.tableName +
      "</b><table>" +
      (t.partitionKey.map(col2td) ++
      t.clusteringColumns.map(col2td) ++
      t.regularColumns.map(col2td)).mkString +
      "</table>"


    val connector = CassandraConnector(sparkContext.getConf)

    val ksTableSplit = ksTable.split('.')

    val (k,t) = ksTableSplit.length match {
      case 0 => (None,None)
      case 1 => (Some(ksTableSplit(0)),None)
      case 2 => (Some(ksTableSplit(0)), Some(ksTableSplit(1)))
    }

    val schema = Schema.fromCassandra(connector, k, t)

    schema.tables.map(table2tbl).mkString
   }
}
