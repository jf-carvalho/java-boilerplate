# Introduction

This is a template java project. All basic features were design to offer a plug-n-play development. Here's a list of code features:
- API initialization with Spring Boot
- Clean Architectured code
- Authentication with JWT
- Basic User entity CRUD
- Cache with Redis
- Database connection (MySQL)
- Generic Repository with Hibernate and Criteria Pattern
- Tests with JUnit and Jacoco coverage report
- Database migrations with flyway
- *ACL is about to be made*

The app was built with the following versions:
- Java 21
- Maven 4.0
- Spring 6.1.5
- JUnit Jupiter API 5.10.2
- Mockito 5.11

---

# First run

### Step 1

The first thing to do is to create a copy of `src/main/resources/application.properties.example` in the same path and name it application.properties. That's the environment file that Spring will use to basic configuration. Everytime a developer has to add or edit environment configuration, they must update the .example file.
No modifications should be needed in the new application.properties file to first run the application.

### Step 2

Run `docker compose up -d` to get MySQL and Redis containers running. That's it.

### Step 3

Run `mvn flyway:migrate` to migrate database schema defined in `src/main/resources/db/migration`. Check [flyway migration docs](https://documentation.red-gate.com/flyway/flyway-cli-and-api/concepts/migrations) for better undestranding.

### Step 4

We need to generate secure keys to get Auth0 JWT generation working. Go to `cd src/main/resources/keys` and use openssl to generate the keys:
- `openssl genrsa -out private-key.pem` to generate private key named "private-key.pem"
- `openssl rsa -in private-key.pem -outform PEM -pubout -out public-key.pem` to extract a public key from the private key we generated before

Note that JWT generation needs these files in the exact indicated location with the exact names specified in the example above.

### Step 5

Now je just need to run tests to see if everything is working smoothly:
- `mvn test` to run all tests
- and `mvn jacoco:report` to generate a coverage report. Anh index.html file should be create in `target/site/jacoco/`. You may open it to check the projects test coverage.

### Have fun!

---

# Exploring the code
*This section of the README file is under construction*
- Architecture and Spring usage
  - Configuration
  - Dependency injection
  - Server
  - Controllers and interceptors
- Infrastructure layer
  - Persistence
  - Security
- Application
- Domain
  - Value Objects dependency
