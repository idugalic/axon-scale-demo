# [projects](http://idugalic.github.io/projects)/axon-scale-demo [![Build Status](https://travis-ci.org/idugalic/axon-scale-demo.svg?branch=master)](https://travis-ci.org/idugalic/axon-scale-demo)

This [Axon](https://axoniq.io/) **demo project** demonstrates two different deployment strategies:
 - monolithic (both Spring profiles `command`(**C**QRS) and `query`(C**Q**RS) are activated within one application/service, the final result is one application/service running: `axon-scale-demo`)
 - microservices (only one Spring profile is activated per application/service (`command` or `query`), the final result are two applications/services running: `axon-scale-demo-command` and `axon-scale-demo-command`)

with two different versions:
 - [non-cluster version](#non-cluster-version) (on local host) and/or 
 - [cluster version](#cluster-version-kubernetes) (to local Kubernetes cluster)
 
> Spring profiles are used to separate `command` from `query` components in a simple way, for demo purposes.
In real world scenarios you would split your code in more (maven) modules, or in more dedicated git repositories.

## Non-Cluster version

This scenario covers running of Spring Boot application/s directly on the host machine. There is no cluster, and only one instance of the application/service is running at the same time.

There are two deployment strategies:
 - [monolithic version](#run-monolithic-version)
 - [microservices version](#run-microservices-version)

### Run Axon Server

You can [download](https://download.axoniq.io/axonserver/AxonServer.zip) a ZIP file with AxonServer as a standalone JAR. This will also give you the AxonServer CLI and information on how to run and configure the server.

### Run monolithic version

`command` and `query` Spring profiles are activated, grouping command and query components into one monolithic application.

You can run the following command to start monolithic version locally:
```
$ ./mvnw spring-boot:run -Dspring.profiles.active=command,query
```

> We use H2 SQL database. Web console is enabled, and it should be available on `/h2-console` URL (eg. `http://localhost:8080/h2-console`). Datasource URL: `jdbc:h2:mem:axon-scale-demo-command,query`

**Verify**:
```
$ curl -i -X POST -H 'Content-Type:application/json' -d '{"value" : "1000"}' 'http://localhost:8080/commandcards'
```
```
$ curl http://localhost:8080/querycards
```

> Axon Server dashboard is available here [http://localhost:8024/](http://localhost:8024/)

### Run microservices version

`command` and `query` services/applications are separately deployed. Each service activated appropriate Spring profile (`command` or `query`).

You can run the following commands to start microservices version locally:
```
$ ./mvnw spring-boot:run -Dspring.profiles.active=command -Dserver.port=8081
$ ./mvnw spring-boot:run -Dspring.profiles.active=query -Dserver.port=8082

```

> Each application use its own H2 SQL database. 
> H2 Web console of command application is enabled, and it should be available on `/h2-console` URL (eg. `http://localhost:8081/h2-console`). Datasource URL: `jdbc:h2:mem:axon-scale-demo-command`.
> H2 Web console of query application is enabled, and it should be available on `/h2-console` URL (eg. `http://localhost:8082/h2-console`). Datasource URL: `jdbc:h2:mem:axon-scale-demo-query`.

**Verify**:

```
$ curl -i -X POST -H 'Content-Type:application/json' -d '{"value" : "1000"}' 'http://localhost:8081/commandcards'
```
```
$ curl http://localhost:8082/querycards
```

> Axon Server dashboard is available here [http://localhost:8024/](http://localhost:8024/)

## Cluster version (Kubernetes)

This scenario covers deployment of our containerized (Docker) applications/services to the Kubernetes cluster, so we can scale services better.


There are two deployment strategies:
 - [monolithic version](#deploy-monolithic-version)
 - [microservices version](#deploy-microservices-version)

### Build the Docker image

Build the application image with [Jib](https://github.com/GoogleContainerTools/jib) directly to a Docker daemon. 'Jib' uses the `docker` command line tool and requires that you have docker available on your PATH.

```
$ ./mvnw compile jib:dockerBuild -Dimage=axon-scale-demo
```

> 'Jib' separates your application into multiple layers, splitting dependencies from classes. Now you don’t have to wait for Docker to rebuild your entire Java application - just deploy the layers that changed.

### Deploy Docker stack to Kubernetes

You typically use docker-compose for local development because it can build and works only on a single docker engine. Docker stack and docker service commands require a `Docker Swarm (configured by defaut)` or `Kubernetes cluster`, and they are step towards production.

[Docker Desktop](https://www.docker.com/products/docker-desktop) comes with Kubernetes and the Compose controller built-in, and enabling it is as simple as ticking a box in the settings.

Now, we can use Docker Compose file and native Docker API for [`stacks`](https://docs.docker.com/engine/reference/commandline/stack/) to manage applications/services on local Kubernetes cluster.

#### Deploy monolithic version

`command` and `query` Spring profiles are activated, grouping command and query components into one [monolithic application](docker-compose.monolith.yml).
```
$ docker stack deploy --orchestrator=kubernetes -c docker-compose.monolith.yml axon-scale-demo-stack
```
When you scale an application, you increase or decrease the number of replicas (we set 2). Each replica of your application represents a Kubernetes Pod that encapsulates your application's container(s).

```yaml
services:  
  axon-scale-demo:
    image: axon-scale-demo
    environment:
      - SPRING_PROFILES_ACTIVE=command,query
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-db:5432/axon-scale-demo
      - SPRING_DATASOURCE_USERNAME=demouser
      - SPRING_DATASOURCE_PASSWORD=thepassword
      - SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
      - AXON.AXONSERVER.SERVERS=axon-server
    ports:
      - 8080:8080
    deploy:
      replicas: 2
```
![Monolith on cluster](monolith-cluster.png)

**Verify**

```
$ curl -i -X POST -H 'Content-Type:application/json' -d '{"value" : "1000"}' 'http://localhost:8080/commandcards'
```
```
$ curl http://localhost:8080/querycards
```
> Axon Server dashboard is available here [http://localhost:8024/](http://localhost:8024/)

#### Deploy microservices version

`command` and `query` services/applications are separately deployed. [Each service](docker-compose.microservices.yml) is activating appropriate Spring profile (`command` or `query`).
```
$ docker stack deploy --orchestrator=kubernetes -c docker-compose.microservices.yml axon-scale-demo-stack
```
When you scale an application, you increase or decrease the number of replicas (we set 2). Each replica of your application represents a Kubernetes Pod that encapsulates your application's container(s).

```yaml
services:
  axon-scale-demo-command:
    image: axon-scale-demo
    environment:
      - SPRING_PROFILES_ACTIVE=command
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-db-command:5432/axon-scale-demo-command
      - SPRING_DATASOURCE_USERNAME=demouser
      - SPRING_DATASOURCE_PASSWORD=thepassword
      - SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
      - AXON_AXONSERVER_SERVERS=axon-server
      - SERVER_PORT=8081
    ports:
      - 8081:8081
    deploy:
      replicas: 2
  axon-scale-demo-query:
    image: axon-scale-demo
    environment:
      - SPRING_PROFILES_ACTIVE=query
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-db-query:5432/axon-scale-demo-query
      - SPRING_DATASOURCE_USERNAME=demouser
      - SPRING_DATASOURCE_PASSWORD=thepassword
      - SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
      - AXON_AXONSERVER_SERVERS=axon-server
      - SERVER_PORT=8082
    ports:
      - 8082:8082
    deploy:
      replicas: 2
```
![Microservices on cluster](microservices-cluster.png)

**Verify**

```
$ curl -i -X POST -H 'Content-Type:application/json' -d '{"value" : "1000"}' 'http://localhost:8081/commandcards'
```
```
$ curl http://localhost:8082/querycards
```
> Axon Server dashboard is available here [http://localhost:8024/](http://localhost:8024/)

Feel free to use `kubectl` CLI to explore your Kubernetes cluster:
```
$ kubectl get all
```


### Kubernetes persistent volumes

There are several different types of volumes that are handled by Compose for Kubernetes.

The following Compose snippet declares a service that uses a persistent volume:
```yaml
services:
  axon-server:
    image: axoniq/axonserver
    hostname: axon-server
    environment:
      - AXONSERVER_EVENTSTORE=/eventstore
      - AXONSERVER_CONTROLDB=/controldb
    volumes:
      - axonserver-eventstore:/eventstore
      - axonserver-controldb:/controldb
    ports:
      - '8024:8024'
      - '8124:8124'
volumes:
  axonserver-eventstore:
  axonserver-controldb:
```

A [persistentVolumeClaim]((https://kubernetes.io/docs/concepts/storage/persistent-volumes/)) volume is used to mount a PersistentVolume into a Pod. PersistentVolumes are a way for users to “claim” durable storage (such as a GCE PersistentDisk or an iSCSI volume) without knowing the details of the particular cloud environment.

> This demo is focusing on scaling axon (spring boot) application/s itself. Infrastructure components like AxonServer and Postgres are not in the focus.
Nevertheless it is fair to say that the data that this components collect is saved in a durable way via `PersistentVolume`s keeping us closer to Production.


### Remove Docker stack
```
$ docker stack rm --orchestrator=kubernetes axon-scale-demo-stack
```


## Run tests

This project comes with some rudimentary tests as a good starting
point for writing your own. Use the following command to execute the
tests using Maven:

```
$ ./mvnw test
```

## References and further reading

- [https://docs.axoniq.io/reference-guide](https://docs.axoniq.io/reference-guide/)
- [https://blog.docker.com/2018/12/simplifying-kubernetes-with-docker-compose-and-friends](https://blog.docker.com/2018/12/simplifying-kubernetes-with-docker-compose-and-friends/)
---

[maven]: https://maven.apache.org/ (Maven)
[atomist]: https://www.atomist.com/ (Atomist)
[axon]: https://axoniq.io/ (Axon)
