import Common._
import com.typesafe.sbt.SbtNativePackager.packageArchetype
import xerial.sbt.Pack._
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

// the assembly settings

// using the java server for this application. java_application would be fine, too

// removes all jar mappings in universal and appends the fat jar


test in assembly := {}

// Don't package the scala jars as they are already in spark
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

packageArchetype.java_application

mappings in Universal <<= (mappings in Universal, assembly in Compile) map { (mappings, fatJar) =>
  val filtered = mappings filter { case (file, name) =>  ! name.endsWith(".jar") }
  filtered :+ (fatJar -> ("lib/" + fatJar.getName))
}



//pack <<= pack dependsOn (assembly dependsOn (rebuildIvyXml dependsOn deliverLocal))
//
//packArchive <<= packArchive dependsOn (rebuildIvyXml dependsOn deliverLocal)

//
// TEST DEPENDENCIES
//
//libraryDependencies +=

libraryDependencies ++= Seq(
  "org.spark-project.akka" %% "akka-testkit" % "2.3.4-spark" % "test", // MIT
  "org.apache.spark" %% "spark-core" % sparkVersion.value % "provided" excludeAll // Apache v2
    ExclusionRule(organization = "org.apache.hadoop"),
  "org.apache.spark" %% "spark-streaming" % sparkVersion.value % "provided",      // Apache v2
  "org.apache.spark" %% "spark-sql" % sparkVersion.value % "provided",            // Apache v2
  "org.apache.spark" %% "spark-mllib" % sparkVersion.value % "provided",          // Apache v2
  "org.apache.spark" %% "spark-graphx" % sparkVersion.value % "provided",         // Apache v2
  "org.apache.spark" %% "spark-repl" % sparkVersion.value % "provided" excludeAll // Apache v2
    ExclusionRule(organization = "org.apache.hadoop"),
  "org.apache.spark" %% "spark-hive" % sparkVersion.value % "provided" excludeAll // Apache v2
    ExclusionRule(organization = "org.apache.hadoop"),
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.4.0" % "provided" excludeAll // SPARK-CASSANDRA CONNECTOR DEPENDENCIES
    ExclusionRule(organization = "org.apache.hadoop"),
  "org.apache.hadoop" % "hadoop-client" % "2.3.0" % "provided" excludeAll
    ExclusionRule(organization = "javax.servlet"),
  "com.databricks" % "spark-csv_2.10" % "1.3.0"
)

//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))


//
// CUSTOM TASKS
//

lazy val kill = taskKey[Unit]("Executing the shell script.")

kill := {
  "sh scripts/terminate_spark_kernels.sh".!
}