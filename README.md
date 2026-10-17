# S3 RB Platform starter

This is a starter Maven multi-module project with two separate Spring Boot applications:

- `s3-rb-service`: client-facing WebFlux API. `/ping` is a health check; `/provider?preview=true` returns mock non-live S3/Athena data; `preview=false` (or omitted) returns mock live Redis data.
- `batch-service`: separate Spring Boot application placeholder for the future S3 event -> SQS -> processing -> Redis/S3 publishing pipeline. SQS integration is not implemented yet.

## Requirements
- JDK 21
- Maven 3.9+

## Build from the project root
```bash
mvn clean package
```

## Run S3 RB API
```bash
java -jar s3-rb-service/target/s3-rb-service-1.0.0-SNAPSHOT.jar
```
Test:
```bash
curl -i http://localhost:8080/ping
curl -i 'http://localhost:8080/provider?preview=true'
curl -i 'http://localhost:8080/provider?preview=false'
curl -i 'http://localhost:8080/provider?preview=abc'
```

## Run batch service
```bash
java -jar batch-service/target/batch-service-1.0.0-SNAPSHOT.jar
```

The provider data is currently mocked. No AWS account, S3 bucket, SQS queue, Redis server, or Athena query is accessed by this starter yet.
