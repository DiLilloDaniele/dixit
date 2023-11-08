package grpcService.server.domain.service

import grpcService.server.domain.model.{Game, User}
import grpcService.server.domain.repository.AccessRepository

case class ServerLogic(accessRepository: AccessRepository) {

  var openGames: Set[Game] = Set()

  def registerNewUser(user: User, password: String) = accessRepository.insertNewUser(user.name, password)

  def retrieveUser(userId: String) = accessRepository.selectUser(userId)

  // TODO da gestire lato server e proto
  def updateUser(user: String, pointsToAdd: Int) = accessRepository.addPointsToUser(user, pointsToAdd)

  def removeGame(game: Game) = openGames = openGames - game

  def getGames() = openGames

  def newGame(game: Game) = openGames = openGames + game

  def login(user: User): Boolean = retrieveUser(user.name) match
    case Some(_user) => _user.password == user.password
    case _ => false
    
}
