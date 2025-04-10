# Authentication Service Setup Guide

## Prerequisites
1. Java 24
2. PostgreSQL
3. OAuth2 Credentials (Google and Facebook)

## Database Setup
```sql
CREATE DATABASE auth_db;
CREATE USER auth_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;
```

## OAuth2 Configuration

### Google OAuth2
1. Go to Google Cloud Console
2. Create a new project
3. Enable OAuth2 API
4. Configure OAuth consent screen
5. Create OAuth2 credentials
6. Add authorized redirect URIs:
   - http://localhost:8080/login/oauth2/code/google
   - http://yourdomain.com/login/oauth2/code/google

### Facebook OAuth2
1. Go to Facebook Developers Console
2. Create a new app
3. Add Facebook Login product
4. Configure OAuth settings
5. Add authorized redirect URIs:
   - http://localhost:8080/login/oauth2/code/facebook
   - http://yourdomain.com/login/oauth2/code/facebook

## Environment Variables
Create a .env file in the root directory:
```
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
FACEBOOK_CLIENT_ID=your_facebook_client_id
FACEBOOK_CLIENT_SECRET=your_facebook_client_secret

DB_URL=jdbc:postgresql://localhost:5432/auth_db
DB_USERNAME=auth_user
DB_PASSWORD=your_password

JWT_SECRET_KEY=your_jwt_secret_key
```

## Build and Run
```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```

## Testing
```bash
# Run tests
./mvnw test

# Run integration tests
./mvnw verify
```

## Health Check
After starting the application, verify it's running:
```bash
curl http://localhost:8080/actuator/health
```

## Initial Admin User
The system creates an initial admin user on first startup:
- Email: admin@system.com
- Password: The password will be printed in the logs during first startup

Change the admin password immediately after first login.

## Security Considerations
1. Use HTTPS in production
2. Rotate JWT secret keys regularly
3. Monitor login attempts for suspicious activity
4. Configure proper CORS settings
5. Set appropriate token expiration times