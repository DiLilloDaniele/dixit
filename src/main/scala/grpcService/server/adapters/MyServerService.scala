package grpcService.server.adapters

//da qui usare il ProtoToDomainAdapter per adattare i dati da passare alle porte, senza avere
//il riferimento alla logica di dominio
import akka.stream.Materializer
import grpcService.*
import grpcService.server.domain.ports.RegisterPort
import grpcService.server.domain.ports.InboundPorts
import grpcService.server.domain.service.ServerLogic

import scala.concurrent.Future

class MyServerService(using mat: Materializer, inboundPorts: InboundPorts) extends Server{
  import mat.executionContext

  override def hello(in: HelloMessage): Future[HelloMessage] =
    val name = in.message
    //inboundPorts.registerPort.registerUser()
    Future.successful(HelloMessage("Ciao " + name + "!!!"))

  override def login(in: LoginRequest): Future[LoginResult] = ???

  override def getOpenedGames(in: OpenedGamesRequest): Future[OpenedGames] = ???

  override def register(in: LoginRequest): Future[LoginResult] = ???
  
}
