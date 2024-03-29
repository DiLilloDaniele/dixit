package grpcService.server.adapters

import akka.stream.Materializer
import grpcService.*
import grpcService.server.domain.ports.RegisterPort
import grpcService.server.domain.ports.InboundPorts
import grpcService.server.domain.service.ServerLogic
import grpcService.server.adapters.ProtoToDomainAdapter

import scala.concurrent.Future

// server service implementing the proto messages requests
class MyServerService(using mat: Materializer, inboundPorts: InboundPorts) extends Server{
  import mat.executionContext

  override def hello(in: HelloMessage): Future[HelloMessage] =
    val name = in.message
    Future.successful(HelloMessage("Ciao " + name + "!!!"))

  override def login(in: LoginRequest): Future[LoginResult] =
    val tuple = ProtoToDomainAdapter.protoToUserCredentials(in)
    Future.successful(LoginResult(
      inboundPorts.registerPort.loginUser(tuple._1, tuple._2)
    ))

  override def getOpenedGames(in: OpenedGamesRequest): Future[OpenedGames] =
    Future.successful(ProtoToDomainAdapter.protoToGames(inboundPorts.gamesManagementPort.getAllGames()))

  override def register(in: LoginRequest): Future[LoginResult] =
    val tuple = ProtoToDomainAdapter.protoToUserCredentials(in)
    inboundPorts.registerPort.registerUser(tuple._1, tuple._2)
    Future.successful(LoginResult(true))

  override def closeGame(in: GameMessage): Future[ClosingResponse] =
    inboundPorts.gamesManagementPort.closeGame(in.address, in.forename)
    Future.successful(ClosingResponse())

  override def newGame(in: GameMessage): Future[NewGameResponse] =
    val result = inboundPorts.gamesManagementPort.openNewGame(in.address, in.forename)
    result match
      case true => Future.successful(NewGameResponse())
      case false => Future.failed(new IllegalStateException("Game already created"))

  override def updateUserPoints(in: UserPoints): Future[UpdateResponse] =
    val result: Boolean = inboundPorts.gamesManagementPort.updateUsers(in.userName.toList, in.points.toList)
    Future.successful(UpdateResponse(result))

  override def getUserPoints(in: UserInfo): Future[SingleUserPoints] =
    val username = in.userName
    Future.successful(SingleUserPoints(inboundPorts.gamesManagementPort.getSingleUserPoints(username)))

}
