# Eagle Bank REST API

A RESTful API for Eagle Bank that allows users to manage their accounts and perform banking transactions.

## Technologies Used

- **Java 21**
- **Spring Boot 3.4.10**
    - Spring Web
    - Spring Security (JWT Authentication)
    - Spring Boot Starter Test
- **PostgreSQL** (Database)
- **Flyway** (Database Migration)
- **Lombok** (Code Generation)
- **Docker & Docker Compose** (Containerization)
- **Maven** (Build Tool)

## Prerequisites

Before running this application, ensure you have the following installed:

- Java Development Kit (JDK) 21
- Maven 3.6+
- Docker and Docker Compose
- Git

## Project Structure

```
eagle-bank-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/eaglebank/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       ├── service/
│   │   │       └── EagleBankApiApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/
│       └── java/
│           └── com/eaglebank/
│               ├── config/
│               ├── controller/
│               ├── integration/
│               ├── repository/
│               ├── service/
│               └── EagleBankApiApplicationTests.java
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/jakubje/eaglebank-api.git
cd eaglebank-api
```

### 2. Start PostgreSQL with Docker

The application uses PostgreSQL as its database. Start it using Docker Compose:

```bash
docker-compose up -d
```

This will start a PostgreSQL container with the following configuration:
- **Database Name**: `eaglebank`
- **Username**: `eaglebank_user`
- **Password**: `eaglebank_password`
- **Port**: `5432`

### 3. Build the Application

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Alternatively, you can run the generated JAR file:

```bash
java -jar target/eaglebank-api-0.0.1-SNAPSHOT.jar
```

The API will be available at: `http://localhost:8080`

## API Documentation

### Base URL
```
http://localhost:8080/v1
```

### Implemented Endpoints (Basic Operations)

#### User Management

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| POST | `/v1/users` | Create a new user | No |
| GET | `/v1/users/{userId}` | Fetch user details | Yes |

#### Account Management

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| POST | `/v1/accounts` | Create a new bank account | Yes |
| GET | `/v1/accounts` | List all user's accounts | Yes |
| GET | `/v1/accounts/{accountNumber}` | Fetch account details | Yes |

#### Transaction Management

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| POST | `/v1/accounts/{accountNumber}/transactions` | Create a transaction (deposit/withdrawal) | Yes |
| GET | `/v1/accounts/{accountNumber}/transactions` | List all transactions for an account | Yes |
| GET | `/v1/accounts/{accountNumber}/transactions/{transactionId}` | Fetch transaction details | Yes |

### Authentication

The API uses JWT (JSON Web Token) for authentication.

**Note**: Authentication endpoint details will be added to the OpenAPI specification.

## Example API Usage

### 1. Create a User

```bash
curl -X POST http://localhost:8080/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "password": "testPassword",
    "phoneNumber": "+447700900123",
    "address": {
      "line1": "123 Main Street",
      "town": "London",
      "county": "Greater London",
      "postcode": "SW1A 1AA"
    }
  }'
```

### 2. Authenticate (Get JWT Token)

```bash
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "your-password"
  }'
```

### 3. Create a Bank Account

```bash
curl -X POST http://localhost:8080/v1/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "Personal Savings",
    "accountType": "personal"
  }'
```

### 4. Create a Transaction (Deposit)

```bash
curl -X POST http://localhost:8080/v1/accounts/01234567/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "amount": 100.00,
    "currency": "GBP",
    "type": "deposit",
    "reference": "Initial deposit"
  }'
```

## Database Schema

The application uses the following main entities:

- **Users**: Stores user information
- **Accounts**: Stores bank account details
- **Transactions**: Stores transaction history

## Configuration

### Application Properties

Key configuration in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/eaglebank
spring.datasource.username=eaglebank_user
spring.datasource.password=eaglebank_password
spring.jpa.hibernate.ddl-auto=update
```

## Docker Compose Configuration

The `docker-compose.yml` file includes:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: eaglebank
      POSTGRES_USER: eaglebank_user
      POSTGRES_PASSWORD: eaglebank_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## Testing

Run the test suite:

```bash
mvn test
```

## Error Handling

The API implements comprehensive error handling:

- **400 Bad Request**: Invalid or missing required data
- **401 Unauthorized**: Missing or invalid authentication token
- **403 Forbidden**: User attempting to access another user's resources
- **404 Not Found**: Resource does not exist
- **409 Conflict**: Operation conflicts with current state (e.g., deleting user with active accounts)
- **422 Unprocessable Entity**: Insufficient funds for withdrawal
- **500 Internal Server Error**: Unexpected server error

## Stopping the Application

To stop the PostgreSQL container:

```bash
docker-compose down
```

To stop and remove all data:

```bash
docker-compose down -v
```

## Future Enhancements

The following endpoints are planned for future implementation:

- Update operations (PATCH) for users and accounts
- Delete operations for users and accounts
- Additional authentication endpoints
- Transaction filtering and pagination
- Account balance history
