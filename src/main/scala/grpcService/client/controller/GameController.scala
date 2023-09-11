package grpcService.client.controller

import grpcService.OpenedGames
import grpcService.client.ClientImpl
import grpcService.client.controller.GameControllerObj.SuccessFun

import scala.util.{Failure, Success}

import grpcService.client.actors.utils._

object GameControllerObj:
  type SuccessFun[A] = (A) => Unit

class GameController(val client: ClientImpl) {
  
  var username = ""

  def getGames(success: SuccessFun[List[String]]): Unit = client.getAvailableGames(success)

  def login(username: String, password: String, success: SuccessFun[Boolean]): Unit = client.login(username, password, success)
  
  def register(username: String, password: String, success: SuccessFun[Boolean]): Unit = client.register(username, password, success)

  def joinGame(address: String) = ???

  def createGame(port: String = "2551", address: String = "127.0.0.1") =
    startupWithRole("foreman", port, address)(???)


}
