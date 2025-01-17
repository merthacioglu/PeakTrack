# PeakTrack

Peak Track is a web-based application where users can schedule 
their workouts and keep track of their fitness progress.

## Features

- Users can sign up and login the application 
- Users can create workouts and add them to their workout list
- Users can update their workout information
- Users can completely delete workouts from their workout list

## Restrictions
- Users cannot register with an already existing username
- Users cannot register with a password that doesn't
satisfy the password requirements
- Users cannot add a workout that has a timing conflict with another workout
in the user's workout list
- Users cannot delete another user's workouts

## Technology Stack

- **Framework:** Spring Boot 3.4.0
- **Java Version:** JDK 21
- **Database:** MySQL 8.0.33 for development, H2 for testing
- **Build Tool:** Maven
- **Other Key Dependencies:**
    - Spring Security with OAuth2 Resource Server
    - Spring Data JPA
    - JWT (JSON Web Token) 0.11.5
    - Lombok
    - SpringDoc OpenAPI (Swagger) 2.8.1
    - Spring Boot Validation
    - Liquibase 4.27.0 for database migration
    - JUnit for testing

## Prerequisites

### Required Software
- Java 21 or higher
- Maven 3.x or higher
- MySQL 8.0.33 or higher

### Configuration Requirements
1. MySQL database server running on port 3306
2. Create an `application-dev.yml` file in `src/main/resources/` with the following configurations:
  - Database connection details:
    - URL
    - Username
    - Password
  - JWT configuration:
    - Secret key
    - Expiration time

3. Environment Variables (optional):
  - `SPRING_PROFILES_ACTIVE`: to specify the active profile (defaults to 'development')
  - `JWT_EXPIRATION_TIME`: to specify JWT token expiration (defaults to 3600000)

4. Liquibase:
  - Ensure the database user has permissions to create and modify database schemas
  - The changelog files should be present in `src/main/resources/db/changelog/`



## Getting Started

### Configuration

1. Clone the repository:
   ```bash
   git clone https://github.com/mhacioglu/peaktrack-server.git
   cd peaktrack-server
   ```

2. Create `application-dev.yml` in `src/main/resources/`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/peaktrack-db?createDatabaseIfNotExist=true
       username: your_mysql_username
       password: your_mysql_password

   security:
     jwt:
       secret-key: your_jwt_secret_key
       expiration-time: jwt_token_expiration_time
   ```

3. Database Setup:
  - Ensure MySQL is running on port 3306
  - The database 'peaktrack-db' will be created automatically if it doesn't exist
  - Liquibase will handle the schema creation and updates automatically

4. Configure Liquibase (optional):
  - Liquibase changelog files are located in `src/main/resources/db/changelog/`
  - To generate a new changelog:
    ```bash
    mvn liquibase:diff
    ```

### Building the Application

```bash
mvn clean install
```

### Running the Application

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. The application will be available at:
  - Base URL: `http://localhost:9090`
  - Swagger UI: `http://localhost:9090/swagger-ui.html`
  - H2 Console (for testing): `http://localhost:9090/h2-console` (when using test profile)

### Verifying the Setup

1. Check the application health:
  - Access Swagger UI to view available endpoints
  - Verify database connectivity through application logs
  - Test user registration and authentication endpoints

2. Common Issues:
  - If database connection fails, verify MySQL credentials and server status
  - If port 9090 is unavailable, modify `server.port` in application.yml
  - For JWT issues, ensure the secret key is properly configured


## API Documentation

The API is secured using JWT authentication. All endpoints except authentication endpoints require a valid JWT token in the Authorization header.

### Authentication Endpoints
- `POST /auth/signup` - Register a new user
    - Requires user registration details in the request body
    - Returns the registered user information

- `POST /auth/login` - Authenticate user and get JWT token
    - Requires username and password in the request body
    - Returns JWT token and expiration time

- `POST /auth/logout` - Logout user and invalidate JWT token
    - Requires valid JWT token in Authorization header
    - Blacklists the current token

### Workout Management
- `GET /api/workout/all` - Get all workouts for the authenticated user
    - Optional query parameters:
        - `from` - Start date-time (Format: yyyy-MM-dd HH:mm)
        - `to` - End date-time (Format: yyyy-MM-dd HH:mm)
    - Returns workouts sorted by start date (newest first)

- `GET /api/workout/generateReport` - Generate workout summary report
    - Returns a list of workout summaries containing:
        - Workout name
        - Start time
        - Duration

- `POST /api/workout/create` - Create a new workout
    - Requires workout details in request body
    - Validates for time conflicts with existing workouts
    - Returns the created workout

- `PUT /api/workout/update` - Update an existing workout
    - Requires workout details with ID in request body
    - Supports partial updates
    - Validates for time conflicts
    - Returns the updated workout

- `DELETE /api/workout/delete/{workoutId}` - Delete a workout
    - Requires workout ID in path
    - Returns 204 No Content on success

### Error Responses
The API uses standard HTTP status codes and returns problem details for errors:
- 400: Bad Request - Invalid input data
- 401: Unauthorized - Invalid credentials
- 403: Forbidden - Invalid/expired token or insufficient permissions
- 404: Not Found - Resource not found
- 409: Conflict - Username already exists
- 500: Internal Server Error

### Interactive Documentation
For detailed request/response schemas and interactive API testing, visit the Swagger UI documentation at:
`http://localhost:9090/swagger-ui/index.html`

Note: All secured endpoints require a valid JWT token in the Authorization header with the Bearer scheme:
```bash
Authorization: Bearer <your-jwt-token>
```


## Testing

### Test Classes

- **AuthControllerTest**: Tests for authentication-related endpoints, 
including user signup, login, and logout.
- **AuthServiceTest**: Tests for the authentication service, including user registration and authentication.
- **CascadeDeletionTests**: Tests for cascade deletion behavior, ensuring that associated workouts are deleted when a user is deleted.
- **PasswordValidatorTest** : Tests for password validation constraints.
- **WorkoutControllerTest**: Tests for workout-related endpoints, including creating, updating, and deleting workouts.
- **WorkoutServiceTest**: Tests for the workout service, including adding, updating, and deleting workouts, as well as listing workouts within a time window.

### Running Tests
To run the tests, use the following command:
```bash
mvn test
```
This will execute all the test cases in the project and provide a summary of the results.
## Project Structure
This Spring Boot project follows a clean architecture pattern 
that separates concerns into distinct layers. 
The main application code resides in src/main, 
containing both Java classes and resources. 
The Java packages are organized into controllers 
for handling HTTP requests, services for business logic,
repositories for data access, and models for data structures. 
The resources directory contains application configuration
and database changelog files. The src/test directory mirrors
this structure to provide comprehensive testing coverage 
for each layer.


```
src/
├── main/
│   ├── java/
│   │   └── org/mhacioglu/peaktrackserver/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       └── model/
│   └── resources/
│       ├── db/
│       │   └── changelog/
│       │       └── db.changelog-master.yaml
│       └── application.yml
└── test/
    └── java/
        └── org/mhacioglu/peaktrackserver/
            ├── controller/
            ├── service/
            └── repository/
```

## Security

The application implements Spring Security with JWT (JSON Web Token) based authentication. Security is configured to protect all endpoints except `/auth/**` and Swagger documentation paths (`/swagger-ui/**`, `/v3/api-docs/**`). The authentication flow begins when users register through `/auth/signup` or login via `/auth/login` endpoints.

Upon successful authentication, the server issues a JWT token with a configurable expiration time. This token must be included in subsequent requests as a Bearer token in the Authorization header. The application maintains token security through a blacklist mechanism, where logged-out tokens are stored until expiration and cleaned up hourly.

CORS (Cross-Origin Resource Sharing) is configured to allow specific origins, with current settings permitting:
- Origin: `http://localhost:8005`
- Methods: GET, POST
- Headers: Authorization, Content-Type

All user passwords are encrypted using BCrypt password encoder before storage. The JWT tokens are signed using HS256 algorithm with a secret key stored in application properties. The system validates each token for authenticity, expiration, and blacklist status before processing any secured request.

The session management is configured as STATELESS, ensuring no session information is stored on the server side, thus improving scalability and security of the application.

## Contribution 
Follow these steps to contribute to the project:

1. Fork the repository
2. Create a feature branch (git checkout -b feature/AmazingFeature)
3. Commit your changes (git commit -m 'Add some AmazingFeature')
4. Push to the branch (git push origin feature/AmazingFeature)
5. Create a new Pull Request

Be sure to follow the project's code style and structure, and write detailed commit messages to describe your changes.


## License
This project is licensed under the [MIT](https://choosealicense.com/licenses/mit/) License.



## Acknowledgments
The project is the implementation of the requirements defined in the following link:
[Workout Tracker Project Idea](https://roadmap.sh/projects/fitness-workout-tracker/solutions?p=1&l=Java)