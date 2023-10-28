package grpcService.server.data.wrapper;

import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class MySqlContainerWrapper {

    MySQLContainer container = (MySQLContainer) new MySQLContainer("mysql:5.7.34").withUsername("root").withPassword("").withEnv("MYSQL_ROOT_HOST", "%");

    public MySQLContainer getContainer() {
        return container;
    }

}
