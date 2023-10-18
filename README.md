# Dixit
This is the project created for the Distributed System LM exam.

### Docker commands to start all services
```bash
docker run -p 3306:3306 --name mysql1 -e MYSQL_ROOT_PASSWORD=root -d mysql --default-authentication-plugin=mysql_native_password -h 127.0.0.1
```