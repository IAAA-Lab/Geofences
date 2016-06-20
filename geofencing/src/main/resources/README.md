Hay que crear la base de datos espacial previamente. Los pasos abriendo una sesión con `psql` son:

```sql
CREATE DATABASE geofencing;
\connect geofencing
CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
CREATE ROLE test WITH LOGIN PASSWORD 'test';
```

Ahora se añade el fichero `application.yml` con el siguiente texto sustituyendo `${secret}` por el clave secreta que se quiera utilizar en el JWT e `${IP}` por la IP donde está la base de dato:

```
# config context path to "/" by setting an empty string
server:
  contextPath:

spring:
  jackson:
    serialization:
      INDENT_OUTPUT: true
  datasource:
    driverClassName: org.postgresql.Driver
    url: jdbc:postgresql://${IP}:5432/geofencing
    username: test
    password: test
  jpa:
    database-platform: org.hibernate.spatial.dialect.postgis.PostgisDialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true

jwt:
  header: Authorization
  secret: ${secret}
  expiration: 604800
  route:
    authentication:
      path: api/users/auth
      refresh: api/users/refresh

logging:
  level:
    org.springframework:
      security: DEBUG
```