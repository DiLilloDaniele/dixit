package domain

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.funspec.AnyFunSpec
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName

import grpcService.server.data.adapters.AccessAdapter

class DomainUnitTest extends AnyFunSpec with Matchers {

    describe("Starting new MySql container") {
        it("should work") {
            var mysql = new MySQLContainer(DockerImageName.parse("mysql:5.7.34"))
            mysql.withExposedPorts(3306, 33060).start()
            /*
                container.getUsername(),
                container.getUserPassword(),
                container.getHost(),
                container.getMappedPort(33060),
                container.getDatabase()
            */
            var username = mysql.getUsername();
            var password = mysql.getPassword();
            var jdbcUrl = mysql.getJdbcUrl();
            var database = mysql.getDatabase();
            val accessAdapter = AccessAdapter("localhost", "3306", "mysql", "", username, password)
            assert(accessAdapter.connectWithoutDbName())
            Thread.sleep(2000)
            accessAdapter.close()
            mysql.stop()

        }
    }

}