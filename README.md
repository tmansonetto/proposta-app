# Proposta App

REST API that receives loan proposals, stores them in PostgreSQL and publishes them to
RabbitMQ so that other microservices can process them. It is the core service of a
loan-proposal system built with Java and Spring Boot.

## How it works

```
Front-end (proposta-web) --POST /proposta--> proposta-app --> PostgreSQL
                                                  |
                                                  +--> RabbitMQ (exchange proposta-pendente.ex)
                                                            |
                                                            +--> proposta-pendente.ms-analise-credito
                                                            +--> proposta-pendente.ms-notificacao
                                                                       |
                                                                       v
                                                            notificacao (microservice)
```

1. The client sends a proposal to `POST /proposta`.
2. The API maps the request to the `Proposta` / `Usuario` entities (MapStruct) and saves it.
3. The proposal is published to the fanout exchange `proposta-pendente.ex`, which copies it
   to every bound queue (publisher confirms and returns are enabled).
4. Other services consume their own queue. The result comes back through the
   `proposta-concluida.*` queues.

## Related services

- [notificacao](https://github.com/tmansonetto/notificacao): notification microservice.
  It consumes the `proposta-pendente.ms-notificacao` and `proposta-concluida.ms-notificacao`
  queues declared by this application. It is currently in early development.

## Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/proposta` | Creates a proposal and publishes it to RabbitMQ |
| `GET` | `/proposta` | Lists the proposals |

Example request body:

```json
{
  "nome": "Maria",
  "sobrenome": "Silva",
  "telefone": "11999999999",
  "cpf": "12345678900",
  "renda": 5000.0,
  "valorSolicitadoFmt": 20000.0,
  "prazoPagamento": 24
}
```

CORS is enabled for `http://localhost` and `http://localhost:4200`.

## Tech stack

- Java 25
- Spring Boot 4.1.2 (Web MVC, Data JPA, AMQP, WebSocket)
- PostgreSQL
- RabbitMQ
- MapStruct and Lombok
- Maven (wrapper included)

## Prerequisites

- JDK 25
- PostgreSQL and RabbitMQ. With Docker:

```bash
docker run -d --name postgres-container -p 5433:5432 -e POSTGRES_PASSWORD=<your-password> postgres
docker exec -it postgres-container psql -U postgres -c "CREATE DATABASE proposta;"
docker run -d --name my-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

RabbitMQ management UI: http://localhost:15672 (`guest` / `guest`, local use only).

## Configuration

`src/main/resources/application.properties` reads the database password from an
environment variable, so no secret is stored in the repository:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/proposta
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
```

Set `DB_PASSWORD` before running (in IntelliJ: *Run > Edit Configurations > Environment variables*).

> `spring.jpa.hibernate.ddl-auto=create-drop` recreates the schema on every start and drops it
> on shutdown. Use `update` if you want to keep the data.

## Running

```bash
# Linux / macOS
DB_PASSWORD=<your-password> ./mvnw spring-boot:run

# Windows (PowerShell)
$env:DB_PASSWORD="<your-password>"; .\mvnw.cmd spring-boot:run
```

The API starts on http://localhost:8080.

## Author

Thiago Mansonetto - [github.com/tmansonetto](https://github.com/tmansonetto)
