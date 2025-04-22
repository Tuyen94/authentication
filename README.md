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
- `POST /api/v1/auth/login` - Authenticate user and get tokens
- `POST /api/v1/auth/logout` - Logout and revoke token

### Token Management
- `POST /api/v1/token/refresh` - Refresh access token
- `POST /api/v1/token/validate` - Validate token and get user info
- `POST /api/v1/token/disable` - Disable/revoke a token

### User Management
- `POST /api/v1/users` - Create user
- `GET /api/v1/users` - Get all users 
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user


## Configuration

Key configuration properties in application.yml:
- Database connection
- JWT secret key and expiration times
- Token settings

## API Examples

### Authentication Endpoints

1. Login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "your_password"
  }'
```
2. Logout:
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer your_access_token"
```
3. Refresh token:
```bash
curl -X POST http://localhost:8080/api/v1/auth/token/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "token": "your_refresh_token_here"
  }'
```

4. Validate token:
```bash
curl -X POST http://localhost:8080/api/v1/auth/token/validate \
  -H "Content-Type: application/json" \
  -d '{
    "token": "your_token_here"
  }'
```

5. Disable token:
```bash
curl -X POST http://localhost:8080/api/v1/auth/token/disable \
  -H "Content-Type: application/json" \
  -d '{
    "token": "your_token_here"
  }'
```

### User Management Endpoints

1. Create a new user:
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "John",
    "lastname": "Doe",
    "email": "john.doe@example.com",
    "password": "your_password",
    "role": "USER"
  }'
```
2. Get all users (Admin only):
```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer your_access_token"
```

3. Get user by ID:
```bash
curl -X GET http://localhost:8080/api/v1/users/{id} \
  -H "Authorization: Bearer your_access_token"
```

4. Update user:
```bash
curl -X PUT http://localhost:8080/api/v1/users/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_access_token" \
  -d '{
    "firstname": "John",
    "lastname": "Smith"
  }'
```

5. Delete user (Admin only):
```bash
curl -X DELETE http://localhost:8080/api/v1/users/{id} \
  -H "Authorization: Bearer your_access_token"
```

Note: Replace `your_access_token`, `your_refresh_token`, and other placeholder values with actual values. The server runs on `localhost:8080` by default - adjust the URL if your server runs on a different host or port.
