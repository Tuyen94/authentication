# Authentication Service

This service provides centralized authentication and user management for the Esoft ecosystem.

## Features

- Token-based authentication using JWT
- User registration and authentication
- Role-based access control
- Token validation and revocation
- User management with CRUD operations
- Secure password handling
- Session management

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/authenticate` - Authenticate user and get tokens
- `POST /api/v1/auth/refresh-token` - Refresh access token
- `POST /api/v1/auth/logout` - Logout and revoke token

### Token Management
- `POST /api/v1/token/validate` - Validate token and get user info
- `POST /api/v1/token/disable` - Disable/revoke a token

### User Management
- `GET /api/v1/users` - Get all users (Admin only)
- `GET /api/v1/users/{id}` - Get user by ID (Admin or self)
- `PUT /api/v1/users/{id}` - Update user (Admin or self)
- `DELETE /api/v1/users/{id}` - Delete user (Admin only)

## Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE auth_db;
```

2. Configure application.yml with database credentials

3. Run the application:
```bash
./mvnw spring-boot:run
```

## Security

- Uses BCrypt for password hashing
- JWT tokens with configurable expiration
- Role-based access control
- Token revocation capabilities
- Stateless authentication

## Dependencies

- Java 24
- Spring Boot
- Spring Security
- PostgreSQL
- JWT
- Lombok

## Configuration

Key configuration properties in application.yml:
- Database connection
- JWT secret key and expiration times
- Token settings