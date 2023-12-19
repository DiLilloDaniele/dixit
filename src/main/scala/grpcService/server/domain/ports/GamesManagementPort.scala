package grpcService.server.domain.ports

import grpcService.server.domain.model.Game
import grpcService.server.domain.service.ServerLogic

class GamesManagementPort(implicit val serverLogic: ServerLogic):

  def getAllGames() = serverLogic.getGames()

  def openNewGame(address: String, name: String): Boolean = serverLogic.newGame(Game(address, name))

  def closeGame(address: String, name: String) = serverLogic.removeGame(Game(address, name))

  def updateUsers(userNames: List[String], points: List[Int]): Boolean = 
    try {
      (userNames.size == points.size) match
        case true => 
          userNames.zip(0 until userNames.size).foreach { (i, j) =>
            serverLogic.updateUser(i, points(j))
          }
        case false => ()
      true
    } catch {
        case e: Exception => false
    }
