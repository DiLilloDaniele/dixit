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
            var mysql = new MySQLContainer("mysql:5.7.34")
            mysql.start()
            /*
                container.getUsername(),
                container.getUserPassword(),
                container.getHost(),
                container.getMappedPort(33060),
                container.getDatabase()
            */
            /*
            var username = mysql.getUsername();
            var password = mysql.getPassword();
            var jdbcUrl = mysql.getJdbcUrl();
            var database = mysql.getDatabase();
            */
            //val accessAdapter = AccessAdapter(mysql.getHost(), "3306", "mysql", "", "root", "root")
            val url = mysql.getJdbcUrl() + "?autoReconnect=true&useSSL=false&enabledTLSProtocols=TLSv1.2"
            println(url)
            val user = mysql.getUsername()
            val pass = mysql.getPassword()
            DriverManager.getConnection(url, user, pass)
            Thread.sleep(2000)
            mysql.stop()

        }
    }

}