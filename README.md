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