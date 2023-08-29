package grpcService.server.domain.service

import grpcService.server.domain.model.User
import grpcService.server.domain.repository.AccessRepository

//deve contenere le outbount ports (gestione dei dati ad esempio)
case class ServerLogic(accessRepository: AccessRepository) {

  val openGames: List[String] = List()

  def apply() = ???

  def registerNewUser(user: User, password: String) = accessRepository.insertNewUser(user.name, password)

  def retrieveUser(userId: String) = accessRepository.selectUser(userId)

  def updateUser(user: User, pointsToAdd: Int) = accessRepository.addPointsToUser(user.name, pointsToAdd)

  def getGames() = ???

  def openNewGame() = ???

  def newGame() = ???

}
