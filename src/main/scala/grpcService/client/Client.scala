package grpcService.client

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import grpcService.{GameMessage, HelloMessage, LoginRequest, OpenedGamesRequest, Server, ServerClient, NewGameResponse, LoginResult, OpenedGames}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import grpcService.client.controller.GameControllerObj
import grpcService.client.controller.GameControllerObj.SuccessFun
import grpcService.client.view.HomepageView

import scala.concurrent.Future

object Client {
  @main def startClient() = {
    // Boot akka
    implicit val sys = ActorSystem("HelloWorldClient")
    implicit val ec = sys.dispatcher

    // Configure the client by code:
    val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8083).withTls(false)

    // Or via application.conf:
    // val clientSettings = GrpcClientSettings.fromConfig(GreeterService.name)

    // Create a client-side stub for the service
    val client: Server = ServerClient(clientSettings)
/*
    val reply = client.hello(HelloMessage("Daniele"))
    reply.onComplete {
      case Success(msg) =>
        println(s"got single reply: $msg")
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
*/
  }
  
  def main(args: Array[String]): Unit =
    val client = ClientImpl()
    
  
}

class ClientImpl() {
 
  implicit val sys: ActorSystem = ActorSystem("HelloWorldClient")
  implicit val ec: ExecutionContextExecutor = sys.dispatcher

  val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8083).withTls(false)

  val client: Server = ServerClient(clientSettings)

  def helloMessage(string: String): Future[HelloMessage] = client.hello(HelloMessage(string))

  def getAvailableGames(success: SuccessFun[List[String]]): Future[OpenedGames] =
    val reply = client.getOpenedGames(OpenedGamesRequest())
    reply.onComplete {
      case Success(msg) =>
        success(msg.clusterName.toList)
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
    reply
    
  def login(username: String, password: String, success: SuccessFun[Boolean]): Future[LoginResult] =
    val reply = client.login(LoginRequest(username, password))
    reply.onComplete {
      case Success(msg) =>
        success(msg.response)
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
    reply
    
  def register(username: String, password: String, success: SuccessFun[Boolean]): Future[LoginResult] =
    val reply = client.register(LoginRequest(username, password))
    reply.onComplete {
      case Success(msg) =>
        success(msg.response)
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
    reply

  def createGame(address: String, user: String, success: SuccessFun[Boolean]): Future[NewGameResponse] =
    val reply = client.newGame(GameMessage(address, user))
    reply.onComplete {
      case Success(msg) =>
        success(true)
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
    reply

}
