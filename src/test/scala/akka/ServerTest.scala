package akka

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.concurrent.ScalaFutures.*
import scala.concurrent.Future

import java.sql.{Connection, DriverManager, ResultSet, SQLException, Statement}

import grpcService.client.ClientImpl
import grpcService.server.applicationService.*
import grpcService.{HelloMessage, SingleUserPoints, NewGameResponse, OpenedGames, LoginResult, ClosingResponse, UserPoints, UpdateResponse}
import akka.grpc.GrpcServiceException

import grpcService.server.data.wrapper.MySqlContainerWrapper
import grpcService.server.data.adapters.AccessAdapter
import grpcService.server.data.ports.AccessPort

import scala.concurrent.{Await}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import concurrent.duration.DurationInt
import scala.language.postfixOps
// set SCALACTIC_FILL_FILE_PATHNAMES=yes on windows shell
class ServerTest extends AnyFunSpec with BeforeAndAfterAll with Matchers {

    var mysql = new MySqlContainerWrapper().getContainer()
    mysql.start()
    val url = mysql.getJdbcUrl() + "?autoReconnect=true&useSSL=false&enabledTLSProtocols=TLSv1.2"
    val user = mysql.getUsername()
    val pass = mysql.getPassword()
    val accessAdapter = AccessAdapter(username = user, password = pass, connectionStringExt = url)
    val server = Service.createService(accessAdapter)

    override def beforeAll(): Unit =
        accessAdapter.connectWithUrl(url, user, pass)
        accessAdapter.createDb()
        accessAdapter.createTable()

    override def afterAll(): Unit = 
        server.close()
        mysql.stop()

    describe("Assuming the gRPC server service started") {
        describe("when the client opens") {
            it("should send correctly an hello message to the server and receive correctly the response") {
                val value = "value"
                Thread.sleep(5000)
                
                val client = ClientImpl()
                
                val future: Future[HelloMessage] = client.helloMessage(value)
                assert(future.isReadyWithin(2000 millis))
                whenReady(future) { s =>
                    s shouldBe HelloMessage("Ciao " + value + "!!!")
                }
            }
            describe("and sends a new game request") {
                it("should receive the new game response") {
                    val client = ClientImpl()
                    
                    val future: Future[NewGameResponse] = client.createGame("address", "user", (bool) => {})
                    assert(future.isReadyWithin(5000 millis))
                    whenReady(future) { s =>
                        s shouldBe NewGameResponse()
                    }
                }
                
            }
            describe("and sends request to get all opened games") {
                it("should receive the list of opened games") {
                    val client = ClientImpl()
                    
                    val future: Future[OpenedGames] = client.getAvailableGames((bool) => {})
                    assert(future.isReadyWithin(5000 millis))
                    whenReady(future) { s =>
                        s shouldBe OpenedGames(List("user"))
                    }
                }
                
            }
            describe("and sends request to register") {
                it("should receive the confirmation registration response") {
                    val client = ClientImpl()
                    
                    var future: Future[LoginResult] = client.register("user","pass",(bool) => {})
                    assert(future.isReadyWithin(5000 millis))
                    whenReady(future) { s =>
                        s shouldBe LoginResult(true)
                    }

                    var retrieveUser: Future[SingleUserPoints] = client.getUserPoints("user",(bool) => {})
                    assert(retrieveUser.isReadyWithin(5000 millis))
                    whenReady(retrieveUser) { s =>
                        s shouldBe SingleUserPoints(0)
                    }
                    
                    future = client.login("user","pass",(bool) => {})
                    assert(future.isReadyWithin(5000 millis))
                    whenReady(future) { s =>
                        s shouldBe LoginResult(true)
                    }

                    val failGameResponse: Future[NewGameResponse] = client.createGame("address", "user", (bool) => {})
                    assert(failGameResponse.failed.futureValue.isInstanceOf[GrpcServiceException])
                    

                    val initialClosingResponse: Future[ClosingResponse] = client.closeGame("address", "user", (bool) => {})
                    assert(initialClosingResponse.isReadyWithin(5000 millis))
                    whenReady(initialClosingResponse) { s =>
                        s shouldBe ClosingResponse()
                    }

                    val gameResponse: Future[NewGameResponse] = client.createGame("address", "user", (bool) => {})
                    assert(gameResponse.isReadyWithin(5000 millis))
                    whenReady(gameResponse) { s =>
                        s shouldBe NewGameResponse()
                    }

                    val closingResponse: Future[ClosingResponse] = client.closeGame("address", "user", (bool) => {})
                    assert(closingResponse.isReadyWithin(5000 millis))
                    whenReady(closingResponse) { s =>
                        s shouldBe ClosingResponse()
                    }

                    val updateResponse: Future[UpdateResponse] = client.updateUsersPoints(List("user"), List(20), (bool) => {})
                    assert(updateResponse.isReadyWithin(5000 millis))
                    whenReady(updateResponse) { s =>
                        s shouldBe UpdateResponse(true)
                    }
                }
                
            }
            describe("and sends request to login without registration") {
                it("should receive the login error") {
                    val client = ClientImpl()
                    
                    val future: Future[LoginResult] = client.login("test","pass",(bool) => {})
                    assert(future.isReadyWithin(2000 millis))
                    whenReady(future) { s =>
                        s shouldBe LoginResult(false)
                    }
                }
                
            }
        }
        describe("the service locator") {
            it("shout retrieve the correct infos to link to the database") {
                val accessAdapter: AccessAdapter = ServiceLocator.getDataAdapter()
                assert(accessAdapter.url == "127.0.0.1")
                assert(accessAdapter.port == "6033")
                assert(accessAdapter.driver == "mysql")
                assert(accessAdapter.dbName == "DIXIT")
                assert(accessAdapter.username == "user_name")
                assert(accessAdapter.password == "root_password")
            }
        }
    }
}