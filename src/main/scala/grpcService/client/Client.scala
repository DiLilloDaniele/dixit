package grpcService.client

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import grpcService.{HelloMessage, Server, ServerClient}
import scala.util.{ Failure, Success }

object Client {
  def main(args: Array[String]): Unit = {
    // Boot akka
    implicit val sys = ActorSystem("HelloWorldClient")
    implicit val ec = sys.dispatcher

    // Configure the client by code:
    val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8080).withTls(false)

    // Or via application.conf:
    // val clientSettings = GrpcClientSettings.fromConfig(GreeterService.name)

    // Create a client-side stub for the service
    val client: Server = ServerClient(clientSettings)

    val reply = client.hello(HelloMessage("Daniele"))
    reply.onComplete {
      case Success(msg) =>
        println(s"got single reply: $msg")
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
  }
}
