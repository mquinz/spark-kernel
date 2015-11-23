/*
 * Copyright 2014 IBM Corp.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ibm.spark.kernel.protocol.v5.handler

import com.ibm.spark.comm.{CommRegistrar, CommStorage, KernelCommWriter}
import com.ibm.spark.kernel.protocol.v5.content.{ShutdownReply, ShutdownRequest, CommOpen}
import com.ibm.spark.kernel.protocol.v5.kernel.{ActorLoader, Utilities}
import com.ibm.spark.kernel.protocol.v5._
import com.ibm.spark.utils.MessageLogSupport
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, future}

/**
 * Represents the handler to shutdown the kernel
 *
 * @param actorLoader The actor loader to use for actor communication
 */
class ShutdownHandler(
  actorLoader: ActorLoader
) extends BaseHandler(actorLoader) with MessageLogSupport
{
  override def process(kernelMessage: KernelMessage): Future[_] = future {
    logKernelMessageAction("Initiating Shutdown request for", kernelMessage)

    val shutdownReply = ShutdownReply(false)

    val replyHeader = Header(
      java.util.UUID.randomUUID.toString,
      "",
      java.util.UUID.randomUUID.toString,
      ShutdownReply.toTypeString,
      "")

    val kernelResponseMessage = KMBuilder()
      .withIds(kernelMessage.ids)
      .withSignature("")
      .withHeader(replyHeader)
      .withParent(kernelMessage)
      .withContentString(shutdownReply).build

    logger.debug("Attempting graceful shutdown.")
    actorLoader.load(SystemActorType.KernelMessageRelay) ! kernelResponseMessage
    System.exit(0)
  }

}

