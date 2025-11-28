# Backend proyecto "Receta electrónica"

## ENUNCIADO

En la carpeta `doc` se pueden encontrar los documentos con las instrucciones y planificación del proyecto ([`doc/tareas_sprint.md`](doc/tareas_sprint.md)) y con las especificaciones del mismo ([`doc/historias.md`](doc/historias.md)).

## PREVIO
### Requisitos previos

* Servidor de BD MySQL
* Maven (versión > 3.5.x)
* (Opcional) GIT
* (Opcional) IDE Java (Eclipse, Netbeans, IntelliJ)

**Nota:** En los equipos de laboratorio, puede ser conveniente establecer la variable de entorno `JAVA_HOME`, para que el comando `mvn` (Maven) compile y ejecute los proyectos siempre con el mismo JDK. 

```sh
export JAVA_HOME=/usr/lib/jvm/jdk-20
export PATH=$JAVA_HOME/bin:$PATH
```

### Crear BD para los ejemplos (si no se ha hecho antes)

* Crear BD "recetas" en MySQL 

```sh
mysql -u root -p    [pedirá la contraseña de MySQL]

mysql> create database recetas;
mysql> create user recetas@localhost identified by "recetas";
mysql> grant all privileges on recetas.* to recetas@localhost;
```

Adicionalmente, puede ser necesario establecer un formato de fecha compatible:
```sql
mysql> set @@global.time_zone = '+00:00';
mysql> set @@session.time_zone = '+00:00';
```

## CREACION DEL PROYECTO SPRING BOOT (ya hecho)
Existen varias alternativas:
* Crear un proyecto Maven vacío e incluir las dependencias de los _starters_ de Spring Boot
* Usar Spring Tool Suite ([https://spring.io/tools](https://spring.io/tools)) y crear un nuevo proyecto _Spring Starter Project_
* Crear el proyecto desde Spring Initializr ([https://start.spring.io/](https://start.spring.io/))

### Características del proyecto
```
Project: Maven Project
Language: Java
Spring Boot version: 3.3.5

Proyecto: 
   groupId: es.uvigo.dagss
   artefactId: recetas
   package: es.uvigo.mei.dagss
   name: Recetas

Packaging: Jar
Java version: 17

Dependencias a incluir:
    Spring Web
    Spring Data JPA
    MySQLDriver
    Spring HATEOAS  (opc)
    Lombook  (opc)
    REST Repositoriess (opc)
```

### Propiedades de conexión con BD

En `src/main/resources/application.properties`

```sh
nano src/main/resources/application.properties


spring.datasource.url=jdbc:mysql://localhost:3306/recetas
spring.datasource.username=recetas
spring.datasource.password=recetas
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.show_sql=true
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImplle
```

**Nota:** El parámetro `spring.jpa.hibernate.ddl-auto` con el valor `update` comprobará si la definición de las tablas de la BD se corresponde con las entidades JPA del proyecto
- Ajustarña el esquema de la BD si fuera necesario, añadiendo nuevas tablas o columnas, pero no eliminado columnas, tablas o restricciones existentes.
- Si se han hecho cambios grandes en las entidades del proyecto, puede ser conveniente eliminar completamente la BD y dejar que JPA la cree de nuevo.

## EJECUCION DEL PROYECTO (desde linea de comandos)

Con Maven, usando con el _goal_ `spring-boot:run` del plugin de Spring Boot

```sh
mvn package
mvn spring-boot:run
```

Directamente, empleando el JAR autoejecutable
```sh
mvn package
java -jar target/recetas-0.1.jar
```
