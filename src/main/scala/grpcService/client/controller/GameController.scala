package grpcService.client.controller

import akka.actor.typed.{ActorSystem}

import grpcService.OpenedGames
import grpcService.client.ClientImpl
import grpcService.client.actors.behaviors.{ForemanBehavior, PlayerBehavior}
import grpcService.client.actors.utils
import grpcService.client.controller.GameControllerObj.SuccessFun

import scala.util.{Failure, Success}
import grpcService.client.actors.utils.Utils

object GameControllerObj:
  type SuccessFun[A] = (A) => Unit

given Conversion [Int , String ] with
  def apply (t: Int): String =
    t.toString

class GameController(val client: ClientImpl, val address: String = "127.0.0.1", val connectionPort: Int = 2551) {
  
  var username = ""
  var foreman: Option[ActorSystem[ForemanBehavior.Command]] = Option.empty

  def getGames(success: SuccessFun[List[String]]): Unit = client.getAvailableGames(success)

  def login(username: String, password: String, success: SuccessFun[Boolean]): Unit = 
    this.username = username
    client.login(username, password, success)
  
  def register(username: String, password: String, success: SuccessFun[Boolean]): Unit = client.register(username, password, success)

  def updateUserPoints(success: SuccessFun[Int]) =
    username match
      case "" => ()
      case v if v != "" => client.getUserPoints(username, success)

  val updateUserPoints = (points: Int) => {
    username match
      case "" => ()
      case v => val fut = client.updateUsersPoints(List(v), List(points), (bool) => ())
  }

  def joinGame(clusterAddress: String) = 
    val future = Utils.startupWithRole("player", connectionPort, address, clusterAddress)(PlayerBehavior(onStop = updateUserPoints)).getWhenTerminated
    future.whenComplete((done, reject) => {
      println("SISTEMA TERMINATO")
    })

  def closeAlreadyOpenedGame() = 
    foreman match
      case Some(value) => 
        println("ORA TERMINO IL SISTEMA")
        value.terminate()
      case _ => ()
    foreman = Option.empty
    client.closeGame(s"$address", username, (_) => ())

  def closeGameFun(address: String, port: String): () => Unit = () => client.closeGame(s"$address:$port", username, (_) => ())
 
  def createGame() =
    if(username != "")
      println("CREO IL GIOCO.....")
      client.createGame(s"$address", username, (_) => {
        println("GIOCO CREATO")
        foreman = Option(
          Utils.startupWithRole("foreman", 2551, address, username)(ForemanBehavior(closeHandler = closeGameFun(address, connectionPort)))
        )
        val future = Utils.startupWithRole("player", 2552, address, username)(PlayerBehavior(onStop = updateUserPoints)).getWhenTerminated
        future.whenComplete((done, reject) => {
          closeAlreadyOpenedGame()
          println("SISTEMA TERMINATO")
        })
      })


}
