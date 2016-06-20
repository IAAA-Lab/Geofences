Hay que crear la base de datos espacial previamente. Los pasos abriendo una sesión con `psql` son:

```sql
CREATE DATABASE geofencing;
\connect geofencing
CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
CREATE ROLE test WITH LOGIN PASSWORD 'test';
```

Ahora se crea un fichero `application.properties` con el siguiente texto sustituyendo `${IP}` por la IP donde está la base de datos:

```
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://${IP}:5432/geofencing
spring.datasource.username=test
spring.datasource.password=test
spring.jpa.database-platform=org.hibernate.spatial.dialect.postgis.PostgisDialect
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.=ERROR
```