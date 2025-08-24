# Social Media REST API

***A simple RESTFull API built with Spring Boot, Hibernate (JPA), and H2 Database (in-memory for development).
The application allows users to:***

Create and retrieve users

Create and view posts

Follow/unfollow other users

Like/unlike posts, check like status, and count likes
________________________________________________________

### This project is intended as a simple educational example, with unit and integration tests and CI enforcing test coverage.

Features

Users

Create a user

Get user by ID or by username

Posts

Create a post (title + body + author)

Get post by ID

List posts by author

View feed (posts by people you follow)

Follows

Follow another user

Unfollow a user

List followers / following

Likes

Like / unlike a post

Count likes for a post

Check if a user liked a post
________________________________________________________________________

## Tech stack

Java 21

Spring Boot 3

Spring Data JPA (Hibernate)

H2 (in-memory DB, console at /h2-console)

Validation (Jakarta)

JUnit 5 + Mockito + MockMvc

JaCoCo for coverage (≥80% threshold in CI)

GitHub Actions CI

Getting started
Prerequisites

Java 21+

Maven 3.9+

Run locally

## Clone and start:

git clone https://github.com/<your-org>/<your-repo>.git
cd <your-repo>
mvn spring-boot:run


The app will start on http://localhost:8080
.

H2 console

Visit http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:socialdb

User: admin
Password: admin

### Example API usage
Create a user
curl -X POST http://localhost:8080/api/users \
-H "Content-Type: application/json" \
-d '{"username":"alice"}'

Create a post
curl -X POST http://localhost:8080/api/posts \
-H "Content-Type: application/json" \
-d '{"authorId":1,"title":"Hello","body":"My first post"}'

Follow a user
curl -X POST http://localhost:8080/api/follows \
-H "Content-Type: application/json" \
-d '{"followerId":1,"followedId":2}'

Like a post
curl -X POST http://localhost:8080/api/likes \
-H "Content-Type: application/json" \
-d '{"userId":1,"postId":5}'

### Tests

Run all tests:

mvn clean verify


This also generates a coverage report:

HTML report: target/site/jacoco/index.html

CI enforces coverage ≥80%.

### CI/CD

GitHub Actions workflow is defined in .gitHub/workflows/ci.yml.
On every push/PR:

Build & test with Maven

Enforce coverage ≥80% with JaCoCo

Upload coverage report as an artifact

## License

MIT — feel free to use this project as a learning resource.
