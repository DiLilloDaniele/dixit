package grpcService.server.domain.service

import grpcService.server.domain.model.{Game, User}
import grpcService.server.domain.repository.AccessRepository

object ServerLogic:

  @main def testSet() =
    val opt = Option.empty
    opt match
      case Some(_user) => print(_user)
      case _ => print("utente non esistente")

case class ServerLogic(accessRepository: AccessRepository) {

  var openGames: Set[Game] = Set(Game("127.0.0.1:8082", "Daniele"))

  def registerNewUser(user: User, password: String) = accessRepository.insertNewUser(user.name, password)

  def retrieveUser(userId: String) = accessRepository.selectUser(userId)

  def updateUser(user: User, pointsToAdd: Int) = accessRepository.addPointsToUser(user.name, pointsToAdd)

  def removeGame(game: Game) = openGames = openGames - game

  def getGames() = openGames

  def newGame(game: Game) = openGames = openGames + game

  def login(user: User): Boolean = accessRepository.selectUser(user.name) match
    case Some(_user) => _user.password == user.password
    case _ => false
    
}
