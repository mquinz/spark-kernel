package integration

import java.io.ByteArrayOutputStream

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.ibm.interpreter.{ScalaInterpreter, InterpreterActor}
import com.typesafe.config.ConfigFactory
import org.apache.log4j.spi.NOPLogger
import org.apache.spark.{SparkContext, SparkConf}
import org.scalatest.{BeforeAndAfter, Matchers, FunSpecLike}

import com.ibm.kernel.protocol.v5.content._
import com.ibm.kernel.protocol.v5._
import org.slf4j.Logger

object InterpreterWithActorSpec {
  val config = """
    akka {
      loglevel = "WARNING"
    }"""
}

class IntepreterWithActorSpec extends TestKit(
  ActorSystem(
    "InterpreterWithActorSpec",
    ConfigFactory.parseString(InterpreterWithActorSpec.config)
  )
) with ImplicitSender with FunSpecLike with Matchers with BeforeAndAfter
{

  private val outputStream = new ByteArrayOutputStream()
  private val interpreter = ScalaInterpreter(List(), outputStream)

  private val conf = new SparkConf()
    .setMaster("local[*]")
    .setAppName("Test Kernel")

  private var context: SparkContext = _

  before {
    outputStream.reset()
    interpreter.start()

    val intp = interpreter.sparkIMain

    intp.beQuietDuring {
      conf.set("spark.repl.class.uri", intp.classServer.uri)
      //context = new SparkContext(conf) with NoSparkLogging
      context = new SparkContext(conf) {
        override protected def log: Logger =
          org.slf4j.helpers.NOPLogger.NOP_LOGGER
      }
      intp.bind(
        "sc", "org.apache.spark.SparkContext",
        context, List( """@transient"""))
    }
  }

  after {
    context.stop()
    interpreter.stop()
  }

  describe("Interpreter Actor with Scala Interpreter") {
    describe("#receive") {
      it("should return ok if the code is executed successfully") {
        val interpreterActor =
          system.actorOf(Props(
            classOf[InterpreterActor],
            interpreter
          ))

        val executeRequest = ExecuteRequest(
          "val x = 3", false, false,
          UserExpressions(), false
        )

        interpreterActor ! executeRequest

        expectMsgClass(classOf[ExecuteReplyOk])
      }

      it("should return error if the code fails") {
        val interpreterActor =
          system.actorOf(Props(
            classOf[InterpreterActor],
            interpreter
          ))

        val executeRequest = ExecuteRequest(
          "val x =", false, false,
          UserExpressions(), false
        )

        interpreterActor ! executeRequest

        expectMsgClass(classOf[ExecuteReplyError])
      }
    }
  }
}