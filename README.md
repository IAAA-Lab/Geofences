# Geofences Management System

Geofences Management System is a system that provides web services to manage geofences of different shapes.

It has been developed as a Final Project in Software Engineering at the [EINA](https://eina.unizar.es/).


## Info for users

### Using the Geofences Management System
Geofences management system can be run by the command:
```
gradle bootRun
```
### Using the Application Manager
Application Manager can be run by the command:
```
gradle bootRun
```
### Using the Application Verifier
Application Verifier can be run by the command:
```
gradle bootRun
```

## Info for developers

### Building and testing the project
Geofences management system has been built around **gradle**. You can build the project by using:
```
gradle build
```

The project contains a set of unit tests made with JUnit. You can run these tests using:
```
gradle check
```

### PostgreSQL configuration for unit tests
Some unit tests require a database to run. If connection with this database is not available, these tests are going to fail. To replicate the database used for testing, follow the next steps:

Execute the command `psql` and introduce the following lines:

```sql
CREATE DATABASE geofencing;
\connect geofencing
CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
CREATE ROLE test WITH LOGIN PASSWORD 'test';
```

Create a file `application.properties` replacing `${IP}` with your database's IP:

```
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://${IP}:5432/geofencing
spring.datasource.username=test
spring.datasource.password=test
spring.jpa.database-platform=org.hibernate.spatial.dialect.postgis.PostgisDialect
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.=ERROR
```

## License
The source code of this library is licensed under the GNU General Public License version 3.

## Credits
* Developer: [Eduardo Ibáñez Vásquez](https://github.com/EduIbanez)
* Supervisor: Francisco Javier Zarazaga Soria