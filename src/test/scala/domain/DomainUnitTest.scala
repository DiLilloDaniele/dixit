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

import java.sql.{Connection, DriverManager, ResultSet, SQLException, Statement}

class DomainUnitTest extends AnyFunSpec with Matchers {

    describe("Starting new MySql container") {
        it("should work") {
            var mysql = new MySQLContainer("mysql:5.7.34").withEnv("MYSQL_ROOT_HOST", "%").asInstanceOf[MySQLContainer]
            mysql.start()
            val accessAdapter = AccessAdapter()
            val url = mysql.getJdbcUrl() + "?autoReconnect=true&useSSL=false&enabledTLSProtocols=TLSv1.2"
            val user = mysql.getUsername()
            val pass = mysql.getPassword()
            //DriverManager.getConnection(url, user, pass)
            assert(accessAdapter.connectWithUrl(url, user, pass))
            accessAdapter.createDb(url, user, pass)
            //assert(accessAdapter.checkDatabaseExist(url, user, pass))
            mysql.stop()

        }
    }

}