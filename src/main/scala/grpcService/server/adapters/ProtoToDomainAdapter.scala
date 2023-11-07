package grpcService.server.adapters

import grpcService.server.domain.model.Game
import grpcService.{LoginRequest, OpenedGames}

object ProtoToDomainAdapter:
  
  def protoToUserCredentials(req: LoginRequest): Tuple2[String, String] = (req.name, req.password)

  def protoToGames(game: Set[Game]): OpenedGames = OpenedGames(game.map(game => game.foreman).toSeq)
