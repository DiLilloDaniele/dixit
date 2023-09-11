package grpcService.server.domain.ports

import grpcService.server.domain.model.User
import grpcService.server.domain.service.ServerLogic

class RegisterPort(implicit val serverLogic: ServerLogic):

  def registerUser(name: String, password: String) =
    serverLogic.registerNewUser(User(name, 0), password)

  def loginUser(name: String, password: String): Boolean =
    serverLogic.login(User(name = name, password = password))