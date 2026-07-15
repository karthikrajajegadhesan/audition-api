# Audition API

The purpose of this Spring Boot application is to test general knowledge of SpringBoot, Java, Gradle etc. It is created for hiring needs of our company but can be used for other purposes.

## Overarching expectations & Assessment areas

<pre>
This is not a university test. 
This is meant to be used for job applications and MUST showcase your full skillset. 
<b>As such, PRODUCTION-READY code must be written and submitted. </b> 
</pre>

- clean, easy to understand code
- good code structures
- Proper code encapsulation
- unit tests with minimum 80% coverage.
- A Working application to be submitted.
- Observability. Does the application contain Logging, Tracing and Metrics instrumentation?
- Input validation.
- Proper error handling.
- Ability to use and configure rest template. We allow for half-setup object mapper and rest template
- Not all information in the Application is perfect. It is expected that a person would figure these out and correct.
  
## Getting Started

### Prerequisite tooling

- Any Springboot/Java IDE. Ideally IntelliJIdea.
- Java 17
- Gradle 8
  
### Prerequisite knowledge

- Java
- SpringBoot
- Gradle
- Junit

### Importing Google Java codestyle into INtelliJ

```
- Go to IntelliJ Settings
- Search for "Code Style"
- Click on the "Settings" icon next to the Scheme dropdown
- Choose "Import -> IntelliJ Idea code style XML
- Pick the file "google_java_code_style.xml" from root directory of the application
__Optional__
- Search for "Actions on Save"
    - Check "Reformat Code" and "Organise Imports"
```

---
**NOTE** -
It is  highly recommended that the application be loaded and started up to avoid any issues.

---

## Audition Application information

This section provides information on the application and what the needs to be completed as part of the audition application.

The audition consists of multiple TODO statements scattered throughout the codebase. The applicants are expected to:

- Complete all the TODO statements.
- Add unit tests where applicants believe it to be necessary.
- Make sure that all code quality check are completed.
- Gradle build completes sucessfully.
- Make sure the application if functional.

## Submission process
Applicants need to do the following to submit their work: 
- Clone this repository
- Complete their work and zip up the working application. 
- Applicants then need to send the ZIP archive to the email of the recruiting manager. This email be communicated to the applicant during the recruitment process. 

  
---
## Additional Information based on the implementation

This application is a Spring Boot 3 REST API that proxies [JSONPlaceholder](https://jsonplaceholder.typicode.com) for posts and comments. It is structured as a layered service with production-oriented error handling, observability, and quality gates.

### Architecture

```
Client → AuditionController → AuditionService → AuditionIntegrationClient → JSONPlaceholder
              ↘ ExceptionControllerAdvice (RFC 7807 ProblemDetail errors)
              ↘ ResponseHeaderInjector (trace/span response headers)
```

| Layer | Responsibility |
|-------|----------------|
| `web` | REST endpoints, request logging, input validation |
| `service` | Business logic (for example post filtering) |
| `integration` | Remote HTTP calls via `RestTemplate` |
| `configuration` | Jackson, RestTemplate, tracing headers, connection timeouts |
| `common` | Shared logging helpers and application exceptions |

### API endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/posts?userId=&title=` | Returns posts, optionally filtered by `userId` and/or case-insensitive `title` substring |
| `GET` | `/posts/{id}` | Returns a single post |
| `GET` | `/posts/{id}/comments` | Returns a post with embedded comments |
| `GET` | `/comments?postId=` | Returns comments for a post as a separate list |

**Examples**

```bash
curl http://localhost:8080/posts
curl "http://localhost:8080/posts?userId=1"
curl "http://localhost:8080/posts?title=sunt"
curl http://localhost:8080/posts/1
curl http://localhost:8080/posts/1/comments
curl "http://localhost:8080/comments?postId=1"
```

Post IDs must be positive integers. Invalid IDs return `400 Bad Request` with a `ProblemDetail` body.

Swagger UI is available at `/swagger-ui.html` when the application is running.
