# Introduction

This is a template Java project. All basic features were designed to offer a plug-and-play development. The main idea was to build a solid, reliable and safe start-point to develop Java REST APIs. To achieve that, concepts of SOLID and Clean Code and Clean Architecture were applied.

It is always a difficult choice to not hard-code framework features into a project. Obviously, the work is more extensive when you don't - but it always pays back. The advantage of copying-pasting snippets and getting things running in a few minutes frequently becomes a headache when new business requirements are requested. Changing and adapting are challenging enough tasks, such as upgrading and scaling applications. Then the project becomes a hostage of frameworks and third-party tools and tasks that were already hard become impossible. In the other hand, frameworks and libraries offer very handy solutions for a vast of problems. It would be irrational to ignore them all and deliberately choose to never use them no matter what. That's why I chose to use various Spring functionalities alongside with lots of other packages and solutions, but never making my application strongly depending on them.

Certainly, there are always those parts where decoupling is harder than others and at some point I had to make these parts more dependent of framework usage. But you can clearly see that the application is layered, well-designed and very well-tested. Most change requirements would be a no-brainer. Oh, and I'm not a bookworm myself but there's a lot of empirical DDD influence as well.

Here's a list of main code features:
- API initialization with Spring Boot
- Clean Architecture code
- Authentication with JWT (access and refresh tokens)
- Basic User entity CRUD
- Cache with Redis
- Database connection (MySQL)
- Generic Repository with Hibernate and Criteria Pattern
- Tests with JUnit and Jacoco coverage report
- Database migrations with flyway
- Basic ACL with roles and permissions
- AWS S3 integration to manipulate User entity _picture_ attribute

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

Copy the `flyway.conf.example` at the project's root and paste it with the name `flyway.conf`. No modifications should be needed.

Run `mvn flyway:migrate` to migrate database schema defined in `src/main/resources/db/migration`. Check [flyway migration docs](https://documentation.red-gate.com/flyway/flyway-cli-and-api/concepts/migrations) for better understanding.

**Note**: when the `V3_0__Insert_super_user` migration runs, it generates a random password for the super user and prints it in the console. Save it to make your first authenticated API calls. You may change that password afterwards.

### Step 4

We need to generate secure keys to get Auth0 JWT generation working. Go to `cd src/main/resources/keys` and use openssl to generate the keys:
- `openssl genrsa -out private-key.pem` to generate private key named "private-key.pem"
- `openssl rsa -in private-key.pem -outform PEM -pubout -out public-key.pem` to extract a public key from the private key we generated before

Note that JWT generation needs these files in the exact indicated location with the exact names specified in the example above.

### Step 5 (optional)

Now je just need to run tests to see if everything is working smoothly:
- `mvn test` to run all tests
- and `mvn jacoco:report` to generate a coverage report. Anh index.html file should be created in `target/site/jacoco/`. You may open it to check the projects test coverage.

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
  - Storage
- Application
- Domain
  - Value Objects dependency
