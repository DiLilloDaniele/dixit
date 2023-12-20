package grpcService.server.applicationService

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.typesafe.config.ConfigFactory
import grpcService.ServerHandler
import grpcService.server.adapters.MyServerService
import grpcService.server.applicationService.Service
import grpcService.server.data.ports.AccessPort
import grpcService.server.domain.ports.{InboundPorts, RegisterPort, GamesManagementPort}
import grpcService.server.domain.service.ServerLogic
import grpcService.server.data.adapters.AccessAdapter

import scala.concurrent.{ExecutionContext, Future}

object Service {

  def main(args: Array[String]): Unit = {
    val listArgs = args.toList
    if(listArgs.size > 0)
      createService(ipAddress = listArgs(0))
    else
      createService()
  }

  def createService(accessAdapter: AccessAdapter = ServiceLocator.getDataAdapter(), ipAddress: String = "127.0.0.1"): Service = 
    // Important: enable HTTP/2 in ActorSystem's config
    // We do it here programmatically, but you can also set it in the application.conf
    val conf = ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
    val system = ActorSystem("HelloWorld", conf)
    val service = new Service(system, accessAdapter, ipAddress)
    service.run()
    return service
    // ActorSystem threads will keep the app alive until `system.terminate()` is called

}

/**
 * usare using e given a catena
 * qua istanzio porte e logica di dominio
 * in MyServerService voglio come contesto le porte da utilizzare
 * nelle porte voglio come contesto la logica di dominio
 * @param system
 */
class Service(system: ActorSystem, repository: AccessAdapter, ipAddress: String) {
  given serverLogic: ServerLogic = ServerLogic {
    AccessPort {
      repository
    }
  }
  val registerPort = RegisterPort()
  val managementPort = GamesManagementPort()

  def close(): Unit = system.terminate()

  def run(): Future[Http.ServerBinding] = {
    // Akka boot up code
    given sys: ActorSystem = system
    given ec: ExecutionContext = sys.dispatcher
    given inboundPorts: InboundPorts = InboundPorts(registerPort, managementPort)

    // Create service handlers
    val service: HttpRequest => Future[HttpResponse] =
      ServerHandler(new MyServerService())

    // Bind service handler servers to localhost:8080/8081
    val binding = Http().newServerAt(ipAddress, 8083).bind(service)

    // report successful binding
    binding.foreach { binding => println(s"gRPC server bound to: ${binding.localAddress}") }

    binding
  }
}
