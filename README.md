# Dixit
This is the project created for the Distributed System LM exam.

### Docker commands to start all services
```bash
docker run -p 3306:3306 --name mysql1 -e MYSQL_ROOT_PASSWORD=root -d mysql --default-authentication-plugin=mysql_native_password -h 127.0.0.1
```

### Tests and commands
To performs the tests with coverage enabled execute the following commands:
```bash
sbt clean
sbt clean coverage test
```

To create the coverage reports (HTML pages) execute the following command:
```bash
sbt coverageReport
```
To execute a single specific test class, use the following command:
```bash
sbt "testOnly akka.AsyncTestingExampleSpec"
```

To start/stop all necessary services execute the following code:
```bash
docker compose up

docker compose down
```
If you want to access the PhpMyAdmin web-page, after the docker containers deployment, you can use the following browser link:
```bash
localhost:8081
```
and use the following credentials:
- Server: empty
- User: user_name
- Password: root_password

To create the server docker image, use:
```bash
docker build -t sbt_dixit/container -f .\docker_server\Dockerfile .
```

## Usage example
First, you need to run the server (once run the MySql container via docker-compose):
```bash
sbt runServer
```
Later, we can run all the other 4 local clients (specifying also the port to discriminate them):
```bash
sbt "run 127.0.0.1 2553"

sbt "run 127.0.0.1 2554"

sbt "run 127.0.0.1 2555"
```

Note that if you want to execute the server with a different ip address than the localhost, you can use the following start command:
```bash
sbt "runMain grpcService.server.applicationService.Service 192.168.1.10"
```