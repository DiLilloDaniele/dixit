package domain

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.funspec.AnyFunSpec
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName

import grpcService.server.data.adapters.AccessAdapter
import grpcService.server.data.ports.AccessPort
import grpcService.server.domain.model.User

import grpcService.server.data.wrapper.MySqlContainerWrapper

import java.sql.{Connection, DriverManager, ResultSet, SQLException, Statement}

class DatabaseUnitTest extends AnyFunSpec with Matchers {

    describe("Starting new MySql container") {
        it("should create the database correctly") {
            var mysql = new MySqlContainerWrapper().getContainer()
            mysql.start()
            val accessAdapter = AccessAdapter()
            val url = mysql.getJdbcUrl() + "?autoReconnect=true&useSSL=false&enabledTLSProtocols=TLSv1.2"
            val user = mysql.getUsername()
            val pass = mysql.getPassword()
            assert(accessAdapter.connectWithUrl(url, user, pass))
            accessAdapter.createDb()
            assert(accessAdapter.checkDatabaseExist())
            mysql.stop()
        }
        describe("after created the database") {
            it("should execute queries") {
                var mysql = new MySqlContainerWrapper().getContainer()
                mysql.start()
                val url = mysql.getJdbcUrl() + "?autoReconnect=true&useSSL=false&enabledTLSProtocols=TLSv1.2"
                val user = mysql.getUsername()
                val pass = mysql.getPassword()
                val accessAdapter = AccessAdapter(username = user, password = pass, connectionStringExt = url)
                val accessPort = AccessPort(accessAdapter)
                assert(accessAdapter.connectWithUrl(url, user, pass))
                accessAdapter.createDb()
                accessPort.insertNewUser("test", "passTest")
                val userRetrieved: Option[User] = accessPort.selectUser("test")
                assert(userRetrieved.nonEmpty)
                assert(userRetrieved.get.name equals "test")
                mysql.stop()
            }
        }
    }

}