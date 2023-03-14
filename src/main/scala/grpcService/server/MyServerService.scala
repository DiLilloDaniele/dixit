package grpcService.server

import grpcService.{HelloMessage, Server}

import scala.concurrent.Future
import akka.stream.Materializer

class MyServerService(implicit mat: Materializer) extends Server {
  import mat.executionContext

  override def hello(in: HelloMessage): Future[HelloMessage] =
    val name = in.message
    Future.successful(HelloMessage("Ciao " + name + "!!!"))

}
