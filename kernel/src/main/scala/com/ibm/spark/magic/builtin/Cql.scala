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

import org.apache.spark.SparkConf
import com.datastax.spark.connector.cql.CassandraConnector
import scala.collection.JavaConversions._

import java.io.PrintStream

import com.google.common.base.Strings
import com.ibm.spark.kernel.protocol.v5.MIMEType
import com.ibm.spark.magic._
import com.ibm.spark.magic.dependencies.{IncludeSparkContext, IncludeOutputStream}
import com.ibm.spark.utils.ArgumentParsingSupport
import com.ibm.spark.magic.builtin.CqlHelper._



class Cql extends CellMagic with ArgumentParsingSupport
with IncludeOutputStream with IncludeSparkContext {

  // Lazy because the outputStream is not provided at construction
  private lazy val printStream = new PrintStream(outputStream)

  override def execute(code: String): CellMagicOutput = {
    def printHelpAndReturn: CellMagicOutput = {
      printHelp(printStream, """%%Cql <cql statement>""")
      CellMagicOutput()
    }

    Strings.isNullOrEmpty(code) match {
      case true => printHelpAndReturn
      case false => CellMagicOutput(MIMEType.TextHtml -> htmlFromCql(code))
    }
  }

  def htmlFromCql(code: String) =  {
    val connector = CassandraConnector(sparkContext.getConf)

    "<table>" + connector.withSessionDo{
      session => {val rows = session.execute(code)
        val cols = rows.getColumnDefinitions
        "<tr>" + (for (col <- cols) yield "<th>" + col.getName + "</th>" ).mkString + "</tr>" +
          (for (row <- rows)
            yield "<tr>"
              + (for (col <- cols) yield "<td>" + row.getStringForCol(col) + "</td>").mkString
              + "</tr>"
            ).mkString
      }
    } + "</table>"
  }
}
