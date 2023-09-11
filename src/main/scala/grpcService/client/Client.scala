package grpcService.client

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import grpcService.{HelloMessage, LoginRequest, OpenedGamesRequest, Server, ServerClient}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import grpcService.client.controller.GameControllerObj
import grpcService.client.controller.GameControllerObj.SuccessFun

object Client {
  def main(args: Array[String]): Unit = {
    // Boot akka
    implicit val sys = ActorSystem("HelloWorldClient")
    implicit val ec = sys.dispatcher

    // Configure the client by code:
    val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8083).withTls(false)

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

class ClientImpl() {
 
  implicit val sys: ActorSystem = ActorSystem("HelloWorldClient")
  implicit val ec: ExecutionContextExecutor = sys.dispatcher

  val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8083).withTls(false)

  val client: Server = ServerClient(clientSettings)

  def getAvailableGames(success: SuccessFun[List[String]]) =
    val reply = client.getOpenedGames(OpenedGamesRequest())
    reply.onComplete {
      case Success(msg) =>
        success(msg.clusterName.toList)
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
    
  def login(username: String, password: String, success: SuccessFun[Boolean]) =
    val reply = client.login(LoginRequest(username, password))
    reply.onComplete {
      case Success(msg) =>
        success(msg.response)
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
    
  def register(username: String, password: String, success: SuccessFun[Boolean]) =
    val reply = client.register(LoginRequest(username, password))
    reply.onComplete {
      case Success(msg) =>
        success(msg.response)
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }

}
