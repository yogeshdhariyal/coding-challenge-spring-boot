# GitHub Repository Scorer API

A Spring Boot application that fetches GitHub repositories and scores them based on **stars**, **forks**, and **recent
activity**.
The API allows filtering repositories by programming language and creation date and returns a ranked list with computed
scores.

---

## Features

- Fetch GitHub repositories using the REST API.
- Score repositories using a weighted combination:
    - Stars (logarithmic scale)
    - Forks (logarithmic scale)
    - Freshness (based on last update)
- Filter repositories by programming language.
- Filter repositories by creation date.
- Returns JSON with repository details and computed score.
- Configurable default programming language.
- Configurable results per api call.

---

## Prerequisites

- Java 21
- Maven
- lombok plugin for your IDE
- GitHub Personal Access Token (PAT) with **`repo`** or **`public_repo`** access

---

## Configuration

Add the following to `application.properties`:

```
server.servlet.context-path=/api/v1

github.default.per-page=50
github.default.language=Java
github.url=https://api.github.com/search/repositories
github.token= YOUR_PERSONAL_ACCESS_TOKEN
```

---

## Running the Application
# Using Maven

mvn spring-boot:run

# Using the docker image

```bash
mvn clean install

docker build -t coding-challenge .

docker run -p 8080:8080 coding-challenge
```

---

## API Documentation

Swagger UI is enabled. After running the application, you can access:

```
http://localhost:8080/api/v1/swagger-ui/index.html
```

---

## API Endpoints

### Get Repository Scores

**URL:** `/repositories/scores`  
**Method:** `GET`

**Query Parameters:**

| Parameter      | Type   | Required | Default     | Description                                  |
|----------------|--------|----------|-------------|----------------------------------------------|
| `language`     | String | No       | Java        | Programming language to filter repositories  |
| `createdAfter` | String | No       | 1 month ago | Minimum creation date in `yyyy-MM-dd` format |

**Example Request:**

```http
GET /repositories/scores?language=Kotlin&createdAfter=2025-09-01
```

**Example Response:**

```json
[
  {
    "name": "dsa-code",
    "stars": 44,
    "forks": 67,
    "lastUpdatedAt": "2025-10-01T16:20:00Z",
    "score": 23.169183556437993,
    "language": "Java"
  },
  {
    "name": "springboot-template",
    "stars": 30,
    "forks": 0,
    "lastUpdatedAt": "2025-09-30T06:37:04Z",
    "score": 11.716993602242573,
    "language": "Java"
  }
]
```

---

## How It Works

1. The API calls GitHub repositories filtered by **language** and **createdAfter** date.
2. GitHub returns repositories.
3. Each repository is mapped to `RepositoryResponse`:
    - `name`
    - `stars`
    - `forks`
    - `lastUpdatedAt`
    - `language`
4. **Score calculation** formula:

```
score = (0.5 * log(1 + stars)) + (0.3 * log(1 + forks)) + (0.2 * freshness * 100)
```

Where freshness is:

```text
freshness = 1 / (1 + days_since_last_update)
```

5. Repositories are returned as a **ranked list**.

---

## Notes

- Only **ISO date format (`yyyy-MM-dd`)** is supported for `createdAfter`.
- If the language is invalid or no repositories match, an empty list is returned.
- Score calculation is handled in the `AlgorithmUtil` class for maintainability.
- The API returns up to **50 repositories per request**.

---

## Example CURL Request

```bash
curl "http://localhost:8080/repositories/scores?language=Python&createdAfter=2025-09-01"
```

---

## Dependencies

- Spring Boot
- Lombok
- Jackson
- Unirest
- Swagger / OpenAPI annotations

---
