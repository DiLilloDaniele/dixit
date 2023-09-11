package grpcService.server.domain.model

case class User(val name: String, val points: Int = 0, password: String = "")
