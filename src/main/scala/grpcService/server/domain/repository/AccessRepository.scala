package grpcService.server.domain.repository

import grpcService.server.domain.model.User

trait AccessRepository:

  def insertNewUser(userName: String, password: String): Unit

  def selectUser(userId: String): User

  def addPointsToUser(userId: String, points: Int): Boolean
