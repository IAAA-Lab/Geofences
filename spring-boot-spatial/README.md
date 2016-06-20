Ejemplo utilizando Spring Boot con Hibernate Spatial.


Hay que crear la base de datos espacial previamente. Los pasos abriendo una sesión con `psql` son:

```sql
CREATE DATABASE spring_boot_spatial;
\connect spring_boot_spatial
CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
CREATE ROLE demo WITH LOGIN PASSWORD 'demo';
```