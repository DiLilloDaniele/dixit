package grpcService.client

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import grpcService.*

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import grpcService.client.controller.GameControllerObj
import grpcService.client.controller.GameControllerObj.SuccessFun
import grpcService.client.view.HomepageView

import scala.concurrent.Future

class ClientImpl(serverAddress: String = "127.0.0.1") {
 
  implicit val sys: ActorSystem = ActorSystem("HelloWorldClient")
  implicit val ec: ExecutionContextExecutor = sys.dispatcher

  val clientSettings = GrpcClientSettings.connectToServiceAt(serverAddress, 8083).withTls(false)

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

  def getUserPoints(username: String, success: SuccessFun[Int]): Future[SingleUserPoints] =
    val reply = client.getUserPoints(UserInfo(username))
    reply.onComplete {
      case Success(msg) =>
        success(msg.points)
      case Failure(e) =>
        println(s"Error while getting the current points: $e")
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

  def closeGame(address: String, user: String, success: SuccessFun[Boolean]): Future[ClosingResponse] =
    val reply = client.closeGame(GameMessage(address, user))
    reply.onComplete {
      case Success(msg) =>
        success(true)
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
    reply

  def updateUsersPoints(usernames: List[String], points: List[Int], success: SuccessFun[Boolean]): Future[UpdateResponse] =
    val reply = client.updateUserPoints(UserPoints(usernames.toSeq, points.toSeq))
    reply.onComplete {
      case Success(msg) =>
        success(true)
      case Failure(e) =>
        println(s"Error sayHello: $e")
    }
    reply

}
