package grpcService.client.controller

import grpcService.OpenedGames
import grpcService.client.ClientImpl
import grpcService.client.actors.behaviors.{ForemanBehavior, PlayerBehavior}
import grpcService.client.actors.utils
import grpcService.client.controller.GameControllerObj.SuccessFun

import scala.util.{Failure, Success}
import grpcService.client.actors.utils.Utils

object GameControllerObj:
  type SuccessFun[A] = (A) => Unit

class GameController(val client: ClientImpl) {
  
  var username = ""

  def getGames(success: SuccessFun[List[String]]): Unit = client.getAvailableGames(success)

  def login(username: String, password: String, success: SuccessFun[Boolean]): Unit = 
    this.username = username
    client.login(username, password, success)
  
  def register(username: String, password: String, success: SuccessFun[Boolean]): Unit = client.register(username, password, success)

  val updateUserPoints = (points: Int) => {
    username match
      case "" => ()
      case v => val fut = client.updateUsersPoints(List(v), List(points), (bool) => ())
  }

  def joinGame(address: String) = 
    val future = Utils.startupWithRole("player", "2552", "127.0.0.1", address)(PlayerBehavior(onStop = updateUserPoints)).getWhenTerminated
    future.whenComplete((done, reject) => {
      println("SISTEMA TERMINATO")
    })

  def createGame(port: String = "2551", address: String = "127.0.0.1") =
    if(username != "")
      println("CREO IL GIOCO.....")
      client.createGame(s"$address:$port", username, (_) => {
        println("GIOCO CREATO")
        val foreman = Utils.startupWithRole("foreman", "2551", "127.0.0.1")(ForemanBehavior())
        
        val future = Utils.startupWithRole("player", "2552", "127.0.0.1")(PlayerBehavior(onStop = updateUserPoints)).getWhenTerminated
        future.whenComplete((done, reject) => {
          foreman.terminate()
          println("SISTEMA TERMINATO")
        })
      })


}
