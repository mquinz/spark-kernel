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

import com.google.common.base.Strings
import com.ibm.spark.kernel.protocol.v5.MIMEType
import com.ibm.spark.magic._
import com.ibm.spark.magic.dependencies.{IncludeOutputStream, IncludeSparkContext}
import com.ibm.spark.utils.ArgumentParsingSupport
import CqlHelper.EscapableString


class Sql extends CellMagic with ArgumentParsingSupport
with IncludeOutputStream with IncludeSparkContext {

  // Lazy because the outputStream is not provided at construction
  private lazy val printStream = new PrintStream(outputStream)

  override def execute(code: String): CellMagicOutput = {
    def printHelpAndReturn: CellMagicOutput = {
      printHelp(printStream, """%%sql <cql statement>""")
      CellMagicOutput()
    }

    Strings.isNullOrEmpty(code) match {
      case true => printHelpAndReturn
      case false => CellMagicOutput(MIMEType.TextHtml -> htmlFromSql(code))
    }
  }

  def htmlFromSql(code: String) = {
    val rows = sqlContext.sql(code)

    val types = rows.columns

    val output = "<table><tr>" +
      types.mkString("<th>","</th><th>","</th>") + "</tr>" +
      rows.map("<tr>" + _.toSeq.map(_.toString.escape).mkString("<td>", "</td><td>", "</td>") + "</tr>")
        .collect().mkString +
      "</table>"

    output

  }
}
