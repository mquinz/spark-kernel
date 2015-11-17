import Common._
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

test in assembly := {}

pack <<= pack dependsOn (rebuildIvyXml dependsOn deliverLocal)

packArchive <<= packArchive dependsOn (rebuildIvyXml dependsOn deliverLocal)

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
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.4.0" % "provided" excludeAll // SPARK-CASSANDRA CONNECTOR DEPENDENCIES
    ExclusionRule(organization = "org.apache.hadoop"),
  "org.apache.hadoop" % "hadoop-client" % "2.3.0" % "provided" excludeAll
    ExclusionRule(organization = "javax.servlet")
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