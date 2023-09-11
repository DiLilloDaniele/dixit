package grpcService.server.domain.ports

import grpcService.server.domain.model.Game
import grpcService.server.domain.service.ServerLogic

class GamesManagementPort(implicit val serverLogic: ServerLogic):

  def getAllGames() = serverLogic.getGames()

  def openNewGame(address: String, name: String) = serverLogic.newGame(Game(address, name))

  def closeGame(address: String, name: String) = serverLogic.removeGame(Game(address, name))
