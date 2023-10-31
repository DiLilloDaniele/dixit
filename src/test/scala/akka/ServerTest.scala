package akka

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.concurrent.ScalaFutures.*
import scala.concurrent.Future

import java.sql.{Connection, DriverManager, ResultSet, SQLException, Statement}

import grpcService.client.ClientImpl
import grpcService.server.applicationService.Service
import grpcService.HelloMessage

import concurrent.duration.DurationInt
import scala.language.postfixOps

class ServerTest extends AnyFunSpec with BeforeAndAfterAll with Matchers {

    val server = Service.createService()

    override def afterAll(): Unit = server.close()

    describe("Assuming the gRPC server service started") {
        describe("when the client opens") {
            it("should send correctly an hello message to the server and receive correctly the response") {
                val value = "value"
                Thread.sleep(5000)
                
                val client = ClientImpl()
                
                val future: Future[HelloMessage] = client.helloMessage(value)
                assert(future.isReadyWithin(500 millis))
                whenReady(future) { s =>
                    s shouldBe HelloMessage("Ciao " + value + "!!!")
                }
            }
            describe("and sends a new game request") {
                it("") {
                    
                }
                
            }
        }
    }
}