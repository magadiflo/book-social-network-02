# Dockerizando el backend (Spring Boot)

---

**Referencias**

- [spring-microservices-in-action-2021: Libro de Spring Microservices In Action](https://github.com/magadiflo/spring-microservices-in-action-2021/blob/main/04.welcome-to-docker.md)
- [docker-kubernetes: Andrés Guzmán](https://github.com/magadiflo/docker-kubernetes/blob/main/business-domain/dk-ms-users/README.md)
- [spring-boot-docker: Get Arrays](https://github.com/magadiflo/spring-boot-docker/blob/main/README.md)
- [spring-boot-email](https://github.com/magadiflo/spring-boot-email)

---

## Modificando el application-dev.yml

Antes de crear nuestro `Dockerfile` vamos a realizar algunas modificaciones a nuestro archivo de
configuración `application-dev.yml`. Recordemos que nuestro backend está enviando un email con un código de activación
a todo usuario que se registre en el sistema, pero el servidor de correo que usamos al desarrollar nuestra aplicación
simplemente es para desarrollo. En este apartado vamos a configurar el servidor de correo con una cuenta de `gmail`
para que el mensaje se envíe de manera real al correo del usuario registrado.

````yml
#book-network-backend/src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/db_book_social_network
    username: magadiflo
    password: magadiflo

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true

  mail:
    host: smtp.gmail.com
    port: 587
    username: magadiflo@gmail.com
    password: vzpopostwaqgsljr
    protocol: smtp
    default-encoding: UTF-8
    properties:
      mail:
        mime:
          charset: UTF-8
        smtp:
          connectiontimeout: 5000
          timeout: 3000
          writetimeout: 5000
          auth: true
          starttls:
            enable: true
            required: true

logging:
  level:
    org.hibernate.SQL: DEBUG

application:
  security:
    jwt:
      secret-key: jNFY9S0YoLZ9xizq2V8FG5yMudcZpBKXyLQjSWPbiX8jNFY9S0Y
      expiration: 3600000
  mailing:
    frontend:
      activation-url: http://localhost:4200/auth/activate-account
  file:
    upload:
      photos-output-path: ./uploads
````

**NOTA**
> En vez de estar modificando manualmente las configuraciones, deberíamos usar variables de entorno
> (por ejemplo ${EMAIL_HOST}), de esta manera tendríamos más flexibilidad al cambiar datos de conexión, pero por
> ahora lo dejaremos así.

## Agregando origen al cors

Como estaremos trabajando con contenedores docker, nuestra aplicación de Angular ya no estará en
`http://localhost:4200`, sino más bien, lo cambiaremos a `http://localhost`, por lo que necesitamos modificar el
cors configurado en nuestro backend para que acepte soliticudes de este nuevo dominio.

````java

@Configuration
public class BeansConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuditorAware<Long> auditorAware() { //El nombre de este método "auditorAware" será colocado en la anotación @EnableJpaAuditing
        return new ApplicationAuditAware();
    }

    @Bean
    public CorsFilter corsFilter() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost"));
        configuration.setAllowedHeaders(Arrays.asList(HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION));
        configuration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.PATCH.name()));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(source);
    }
}
````

## Configurando el Dockerfile

Como en este curso estaremos creando un release de nuestra aplicación, es decir un lanzamiento o liberación de una nueva
versión y además estaremos empaquetándolo, necesitamos actualizar la versión de nuestro proyecto. Por defecto la versión
con la que se crea el proyecto de `Spring Boot` es `0.0.1-SNAPSHOT`, por lo que ahora lo cambiaremos para esta nueva
versión a `1.0.0`. Esto lo hacemos para saber que en cada versión hemos realizado cambios e introducido nuevas
características, etc.

En el archivo `pom.xml` de nuestra aplicación de spring boot modificamos la versión para este primer release:

````xml

<version>1.0.0</version>
````

Para crear el `Dockerfile` de nuestra aplicación de `Spring Boot`, tomamos como referencia no solo el curso, sino
también las referencias colocadas al inicio de este documento, ya que anteriormente he venido trabajando en este tipo
de archivos con otras aplicaciones de Spring Boot.

Para resumir, el siguiente `Dockerfile` tiene 3 etapas:

- La primera etapa `build`, es para descargar las dependencias de maven y generar el archivo `.jar` de nuestra
  aplicación.
- La segunda etapa `extraction`, permite extraer capas del `.jar` construido en la etapa anterior. El `.jar` es
  descompuesto de la siguiente manera: `dependencies`, `spring-boot-loader`, `snapshot-dependencies` y `application`.
- La tercera etapa `run`, permite copiar las capas extraídas en la etapa anterior dentro de un directorio personalizado,
  en nuestro caso llamado `/app` y luego definir el comando que será ejecutado cada vez que se cree un contendor de la
  imagen. Normalmente, si no hiciéramos la extracción de las capas del `.jar` y trabajáramos con el `.jar` mismo,
  utilizaríamos el comando para el `CMD ["java", "-jar", "app.jar"]`, pero como estamos trabajando con la extracción de
  capas del `.jar`, el comando anterior ya no nos sirve, dado que ahora no tenemos un `.jar` sino directorios sueltos de
  la aplicación, así que para ejecutar la aplicación debemos utilizar
  el `CMD ["java", "org.springframework.boot.loader.launch.JarLauncher"]`. Ahora, en nuestro Dockerfile final, estamos
  pasándole variables de entorno cada vez que se crea un contenedor, en ese sentido, el `CMD` va a verse tal como se
  muestra en el Dockerfile de abajo.

````Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY ./mvnw ./
COPY ./.mvn ./.mvn
COPY ./pom.xml ./

RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline

COPY ./src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS extraction
WORKDIR /app
COPY --from=build /build/target/*.jar ./app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-alpine AS run
WORKDIR /app
COPY --from=extraction /app/dependencies ./
COPY --from=extraction /app/spring-boot-loader ./
COPY --from=extraction /app/snapshot-dependencies ./
COPY --from=extraction /app/application ./

ARG PROFILE=dev

ENV SPRING_PROFILES_ACTIVE=${PROFILE}
ENV DB_URL=jdbc:postgresql://postgres:5432/db_book_social_network
ENV ACTIVATION_EMAIL_URL=http://localhost/auth/activate-account

EXPOSE 8080
CMD ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dspring.datasource.url=${DB_URL}", "-Dapplication.mailing.frontend.activation-url=${ACTIVATION_EMAIL_URL}", "org.springframework.boot.loader.launch.JarLauncher"]
````

**NOTA**
> Observar que hemos creado una variable de entorno `ENV DB_URL` para definir la cadena de conexión de nuestra base de
> datos, pero en esta cadena de conexión estamos usando el nombre del servicio definido en el archivo `compose.yml` de
> nuestra base de datos llamado `postgres` y además el puerto que estamos usando es el puerto interno del contendor
> `5432`. Nuestra aplicación de Spring Boot se podrá conectar a la base de datos de `postgres`, ya que estamos usando
> la misma red interna `spring-net`.
>
> Otro punto a observar es la variable de entorno `ACTIVATION_EMAIL_URL`, esta variable de entorno define la url que
> irá en el mensaje del correo de activación para que el usuario sea redireccionado al frontend. Si revisamos el backend
> veremos en el archivo `application-dev.yml` que la url para esta configuración está apuntando a `localhost:4200`,
> mientras que en nuestro caso solo a `localhost`. Esto lo hacemos porque nuestra aplicación de Angular estará en el
> servidor `nginx` que estará corriendo en el puerto `80` del navegador, es decir podremos acceder simplemente
> colocando `http://localhost`.

Ahora que ya tenemos construida el `Dockerfile`, vamos a crear la imagen de nuestra aplicación de Spring Boot. Es
importante observar que el `Dockerfile` fue creado en la raíz de la aplicación `book-network-backend`, por lo que
debemos ubicarnos mediante la línea de comandos en dicha ruta.

````bash
M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\18.bouali_ali\08.full_web_application\book-social-network-02\book-network-backend (main -> origin)

$ docker image build -t magadiflo/book-network-backend:1.0.0 .

[+] Building 255.7s (23/23) FINISHED                                                        
 => [internal] load build definition from Dockerfile                                        
 => => transferring dockerfile: 1.00kB                                                      
 => [internal] load metadata for docker.io/library/eclipse-temurin:21-jre-alpine            
 => [internal] load metadata for docker.io/library/eclipse-temurin:21-jdk-alpine            
 => [internal] load .dockerignore                                                           
 => => transferring context: 2B                                                             
 => CACHED [build 1/9] FROM docker.io/library/eclipse-temurin:21-jdk-alpine                 
 => [internal] load build context                                                           
 => => transferring context: 104.99kB                                                       
 => [extraction 1/4] FROM docker.io/library/eclipse-temurin:21-jre-alpine                   
 => [build 2/9] WORKDIR /build                                                              
 => [build 3/9] COPY ./mvnw ./                                                              
 => [build 4/9] COPY ./.mvn ./.mvn                                                          
 => [build 5/9] COPY ./pom.xml ./                                                           
 => [build 6/9] RUN sed -i -e 's/\r$//' ./mvnw                                              
 => [build 7/9] RUN ./mvnw dependency:go-offline                                            
 => [build 8/9] COPY ./src ./src                                                            
 => [build 9/9] RUN ./mvnw clean package -DskipTests                                        
 => CACHED [extraction 2/4] WORKDIR /app                                                    
 => [extraction 3/4] COPY --from=build /build/target/*.jar ./app.jar                        
 => [extraction 4/4] RUN java -Djarmode=layertools -jar app.jar extract                     
 => [run 3/6] COPY --from=extraction /app/dependencies ./                                   
 => [run 4/6] COPY --from=extraction /app/spring-boot-loader ./                             
 => [run 5/6] COPY --from=extraction /app/snapshot-dependencies ./                          
 => [run 6/6] COPY --from=extraction /app/application ./                                    
 => exporting to image                                                                      
 => => exporting layers                                                                     
 => => writing image sha256:e8084784109f7e05e1db1698f977f3a484b78fc11cc200a6ab3cd20e6647feb3
 => => naming to docker.io/magadiflo/book-network-backend:1.0.0                             
````

Verificamos la creación exitosa de nuestra imagen:

````bash
$ docker image ls
REPOSITORY                       TAG             IMAGE ID       CREATED         SIZE
magadiflo/book-network-backend   1.0.0           e8084784109f   2 minutes ago   249MB
````

Ahora, necesitamos crear un contenedor de nuestra imagen, para eso necesitamos modificar el archivo `compose.yml`,
ya que usaremos `docker compose` para crear los contenedores de nuestra aplicación. Anteriormente, ya habíamos venido
trabajando con algunos servicios en nuestro archivo `compose.yml` para crear contenedores `(postgres, mail-dev)`, así
que ahora necesitamos agregar un nuevo servicio para la creación del contenedor de nuestra aplicación. El servicio que
crearemos se llamará `book-network-backend`:

````yml
services:
  postgres:
    image: postgres:15.2-alpine
    container_name: c-postgres-bsn
    restart: unless-stopped
    environment:
      POSTGRES_DB: db_book_social_network
      POSTGRES_USER: magadiflo
      POSTGRES_PASSWORD: magadiflo
    ports:
      - 5435:5432
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - spring-net
    healthcheck:
      test: [ 'CMD-SHELL', 'pg_isready -U magadiflo -d db_book_social_network' ]
      interval: 10s
      timeout: 5s
      retries: 5

  mail-dev:
    image: maildev/maildev
    container_name: c-mail-dev-bsn
    restart: unless-stopped
    ports:
      - 1080:1080
      - 1025:1025
    networks:
      - spring-net

  book-network-backend:
    image: magadiflo/book-network-backend:1.0.0
    container_name: c-book-network-backend
    restart: unless-stopped
    ports:
      - 8080:8080
    networks:
      - spring-net
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  postgres_data:
    name: postgres_data

networks:
  spring-net:
    name: spring-net
````

Ahora que tenemos nuestra imagen construida y el servicio de nuestra aplicación definida en el archivo `compose.yml`,
vamos a posicionarnos mediante la línea de comandos en la raíz del proyecto donde se encuentra el archivo `compose.yml`
y ejecutar el siguiente comando:

````bash
M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\18.bouali_ali\08.full_web_application\book-social-network-02 (main -> origin)

$ docker compose up -d

[+] Running 0/0                             
[+] Running 4/4ng-net  Creating             
 ✔ Network spring-net                Created
 ✔ Container c-mail-dev-bsn          Started
 ✔ Container c-postgres-bsn          Healthy
 ✔ Container c-book-network-backend  Started
````

Verificamos que los contenedores se han creado correctamente:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE                                  COMMAND                  CREATED         STATUS                     PORTS                                            NAMES
9baaf8e4f25c   magadiflo/book-network-backend:1.0.0   "/__cacert_entrypoin…"   3 minutes ago   Up 3 minutes               0.0.0.0:8080->8080/tcp                           c-book-network-backend
82114ad59be4   postgres:15.2-alpine                   "docker-entrypoint.s…"   3 minutes ago   Up 3 minutes (healthy)     0.0.0.0:5435->5432/tcp                           c-postgres-bsn
3febfcdcbb90   maildev/maildev                        "bin/maildev"            3 minutes ago   Up 3 minutes (unhealthy)   0.0.0.0:1025->1025/tcp, 0.0.0.0:1080->1080/tcp   c-mail-dev-bsn
````

Si hacemos un `logs` al contenedor de nuestra aplicación vemos que se ha ejecutado correctamente sin ningún problema:

````bash
$ docker container logs c-book-network-backend

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.6)

2024-06-21T23:37:10.445Z  INFO 1 --- [book-network-backend] [           main] d.m.b.n.a.BookNetworkBackendApplication  : Starting BookNetworkBackendApplication v1.0.0 using Java 21.0.3 with PID 1 (/app/BOOT-INF/classes started by root in /app)
2024-06-21T23:37:10.452Z  INFO 1 --- [book-network-backend] [           main] d.m.b.n.a.BookNetworkBackendApplication  : The following 1 profile is active: "dev"
2024-06-21T23:37:13.140Z  INFO 1 --- [book-network-backend] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2024-06-21T23:37:13.344Z  INFO 1 --- [book-network-backend] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 187 ms. Found 6 JPA repository interfaces.
2024-06-21T23:37:15.370Z  INFO 1 --- [book-network-backend] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-06-21T23:37:15.407Z  INFO 1 --- [book-network-backend] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-06-21T23:37:15.408Z  INFO 1 --- [book-network-backend] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.24]
2024-06-21T23:37:15.533Z  INFO 1 --- [book-network-backend] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-06-21T23:37:15.538Z  INFO 1 --- [book-network-backend] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 4874 ms
2024-06-21T23:37:16.077Z  INFO 1 --- [book-network-backend] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2024-06-21T23:37:16.228Z  INFO 1 --- [book-network-backend] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.4.8.Final
2024-06-21T23:37:16.338Z  INFO 1 --- [book-network-backend] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2024-06-21T23:37:17.036Z  INFO 1 --- [book-network-backend] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2024-06-21T23:37:17.113Z  INFO 1 --- [book-network-backend] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2024-06-21T23:37:17.670Z  INFO 1 --- [book-network-backend] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@2ecdf528
2024-06-21T23:37:17.675Z  INFO 1 --- [book-network-backend] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2024-06-21T23:37:21.133Z  INFO 1 --- [book-network-backend] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2024-06-21T23:37:21.444Z  INFO 1 --- [book-network-backend] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2024-06-21T23:37:22.731Z  INFO 1 --- [book-network-backend] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
2024-06-21T23:37:24.235Z  WARN 1 --- [book-network-backend] [           main] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
2024-06-21T23:37:25.121Z  INFO 1 --- [book-network-backend] [           main] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with [org.springframework.security.web.session.DisableEncodeUrlFilter@24a26847, org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter@30eb427c, org.springframework.security.web.context.SecurityContextHolderFilter@23707d5d, org.springframework.security.web.header.HeaderWriterFilter@558fe3b0, org.springframework.web.filter.CorsFilter@4cbd17b3, org.springframework.security.web.authentication.logout.LogoutFilter@3da20c42, dev.magadiflo.book.network.app.security.JwtAuthFilter@174aabb2, org.springframework.security.web.savedrequest.RequestCacheAwareFilter@7921eb37, org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter@3fdb9c55, org.springframework.security.web.authentication.AnonymousAuthenticationFilter@59c9e3b9, org.springframework.security.web.session.SessionManagementFilter@6253df71, org.springframework.security.web.access.ExceptionTranslationFilter@124bc9fa, org.springframework.security.web.access.intercept.AuthorizationFilter@2c47135c]
2024-06-21T23:37:26.080Z  INFO 1 --- [book-network-backend] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-06-21T23:37:26.102Z  INFO 1 --- [book-network-backend] [           main] d.m.b.n.a.BookNetworkBackendApplication  : Started BookNetworkBackendApplication in 16.637 seconds (process running for 17.365)
2024-06-21T23:37:26.337Z DEBUG 1 --- [book-network-backend] [           main] org.hibernate.SQL                        :
    select
        r1_0.id,
        r1_0.created_date,
        r1_0.last_modified_date,
        r1_0.name
    from
        roles r1_0
    where
        r1_0.name=?
````
