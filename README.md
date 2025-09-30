# File Reconciliation Application

A Spring Boot application for reconciling financial transactions between two CSV files. The application compares transaction data, identifies matches based on a sophisticated scoring algorithm, and reports discrepancies.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
  - [Local Execution](#local-execution)
  - [Docker Deployment](#docker-deployment)
- [API Documentation](#api-documentation)
- [CSV File Format](#csv-file-format)
- [Configuration](#configuration)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

## Features

- **Parallel File Processing**: Processes two CSV files simultaneously using virtual threads for optimal performance
- **Intelligent Matching**: Multi-factor scoring algorithm that considers:
  - Transaction ID
  - Amount (with 1% tolerance)
  - Date (with ±2 days tolerance)
  - Transaction narrative and description
  - Wallet reference
  - Transaction type
- **Duplicate Handling**: Properly handles duplicate transactions with the same ID
- **Comprehensive Reporting**: Detailed reconciliation results with matched and unmatched transactions
- **File Size Validation**: Enforces 10MB maximum file size limit
- **Error Handling**: Robust exception handling with detailed error messages
- **Docker Support**: Containerized deployment with Docker and Docker Compose

## Prerequisites

### For Local Development
- **Java 21** or higher
- **Maven 3.8+**
- Minimum 512MB RAM (recommended 1GB for large files)

### For Docker Deployment
- **Docker 20.10+**
- **Docker Compose 2.0+** (optional, for orchestration)

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd file-comparison
```

### 2. Build the Application

```bash
mvn clean install
```

This will:
- Compile the source code
- Run all unit tests
- Package the application as a JAR file

## Running the Application

### Local Execution

#### Option 1: Using Maven (Development)

```bash
mvn spring-boot:run
```

#### Option 2: Using the JAR File (Production)

```bash
java -jar target/file-comparison-1.0.0.jar
```

#### Option 3: With Custom Configuration

```bash
java -jar target/file-comparison-1.0.0.jar --server.port=8090
```

The application will start on `http://localhost:8080` by default.

You should see output like:
```
Started FileComparisonApplication in 2.345 seconds
```

### Docker Deployment

#### Quick Start with Docker

1. **Build the Docker image:**

```bash
docker build -t file-reconciliation-app:latest .
```

2. **Run the container:**

```bash
docker run -p 8080:8080 file-reconciliation-app:latest
```

The application will be accessible at `http://localhost:8080`.

#### Docker Compose Deployment

1. **Start the application:**

```bash
docker-compose up -d
```

2. **View logs:**

```bash
docker-compose logs -f
```

3. **Stop the application:**

```bash
docker-compose down
```

#### Advanced Docker Options

**Run with custom port:**
```bash
docker run -p 9090:8080 file-reconciliation-app:latest
```

**Run with custom memory limits:**
```bash
docker run -p 8080:8080 -m 1g --memory-reservation 512m file-reconciliation-app:latest
```

**Run with environment variables:**
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e MAX_FILE_SIZE=20MB \
  file-reconciliation-app:latest
```

**Run with volume for logs:**
```bash
docker run -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  file-reconciliation-app:latest
```

**Run in detached mode with auto-restart:**
```bash
docker run -d \
  --name file-reconciliation \
  --restart unless-stopped \
  -p 8080:8080 \
  file-reconciliation-app:latest
```

#### Docker Configuration Files

##### Dockerfile

Create a `Dockerfile` in the project root:

```dockerfile
# Multi-stage build for optimized image size
FROM maven:3.9-eclipse-temurin-21-alpine AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from build stage
COPY --from=build /app/target/file-comparison-*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimization flags
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

##### docker-compose.yml

Create a `docker-compose.yml` in the project root:

```yaml
version: '3.8'

services:
  file-reconciliation:
    build:
      context: .
      dockerfile: Dockerfile
    image: file-reconciliation-app:latest
    container_name: file-reconciliation-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - MAX_FILE_SIZE=10MB
      - LOGGING_LEVEL_ROOT=INFO
      - LOGGING_LEVEL_APP=DEBUG
    volumes:
      - ./logs:/app/logs
      - ./config:/app/config:ro
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    networks:
      - reconciliation-network

networks:
  reconciliation-network:
    driver: bridge
```

##### .dockerignore

Create a `.dockerignore` file to optimize build context:

```
# Maven
target/
!target/file-comparison-*.jar
.mvn/
mvnw
mvnw.cmd

# IDE
.idea/
.vscode/
*.iml
.project
.classpath
.settings/

# Git
.git/
.gitignore

# Logs
logs/
*.log

# Documentation
README.md
docs/

# Test files
src/test/

# OS
.DS_Store
Thumbs.db
```

#### Docker Management Commands

**View running containers:**
```bash
docker ps
```

**View container logs:**
```bash
docker logs -f file-reconciliation
```

**Execute commands in container:**
```bash
docker exec -it file-reconciliation sh
```

**Stop container:**
```bash
docker stop file-reconciliation
```

**Remove container:**
```bash
docker rm file-reconciliation
```

**Remove image:**
```bash
docker rmi file-reconciliation-app:latest
```

**Prune unused resources:**
```bash
docker system prune -a
```

#### Building for Different Environments

**Development:**
```bash
docker build -t file-reconciliation-app:dev --target build .
```

**Production with version tag:**
```bash
docker build -t file-reconciliation-app:1.0.0 .
docker tag file-reconciliation-app:1.0.0 file-reconciliation-app:latest
```

**With build arguments:**
```bash
docker build \
  --build-arg JAR_FILE=target/file-comparison-1.0.0.jar \
  -t file-reconciliation-app:latest .
```

## API Documentation

### Reconcile Transactions

**Endpoint:** `POST /api/v1/reconcile-transactions`

**Content-Type:** `multipart/form-data`

**Parameters:**
- `file1` (required): First CSV file (Paymentology format)
- `file2` (required): Second CSV file (Client format)

**File Constraints:**
- Maximum file size: 10MB per file
- Format: CSV with specific headers (see below)
- Encoding: UTF-8

**Example Request using cURL:**

```bash
curl -X POST http://localhost:8080/api/v1/reconcile-transactions \
  -F "file1=@PaymentologyMarkoffFile20140113.csv" \
  -F "file2=@ClientMarkoffFile20140113.csv"
```

**Example Request with Docker container:**

```bash
# If running locally
curl -X POST http://localhost:8080/api/v1/reconcile-transactions \
  -F "file1=@PaymentologyMarkoffFile20140113.csv" \
  -F "file2=@ClientMarkoffFile20140113.csv"

# If running on remote Docker host
curl -X POST http://<docker-host-ip>:8080/api/v1/reconcile-transactions \
  -F "file1=@PaymentologyMarkoffFile20140113.csv" \
  -F "file2=@ClientMarkoffFile20140113.csv"
```

**Example Request using Postman:**

1. Select `POST` method
2. Enter URL: `http://localhost:8080/api/v1/reconcile-transactions`
3. Go to "Body" tab
4. Select "form-data"
5. Add two keys:
  - Key: `file1`, Type: File, Value: Select your first CSV
  - Key: `file2`, Type: File, Value: Select your second CSV
6. Click "Send"

**Response Example:**

```json
{
  "totalRecordsInFile1": 100,
  "totalRecordsInFile2": 98,
  "matchedRecords": 85,
  "unmatchedRecordsInFile1": 15,
  "unmatchedRecordsInFile2": 13,
  "matchPercentage": 85.0,
  "processingTimeMs": 1234,
  "unmatchedTransactionPairs": [
    {
      "transaction1": {
        "profileName": "Card Campaign",
        "transactionDate": "2014-01-11T22:27:44Z",
        "transactionAmount": -20000.0,
        "transactionNarrative": "*MOLEPS ATM25 MOLEPOLOLE BW",
        "transactionDescription": "DEDUCT",
        "transactionID": "0584011808649511",
        "transactionType": "TYPE_2",
        "walletReference": "P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5"
      },
      "transaction2": null
    }
  ]
}
```

### Health Check Endpoint

**Endpoint:** `GET /actuator/health`

**Response:**
```json
{
  "status": "UP"
}
```

This endpoint is used by Docker healthchecks and monitoring tools.

## CSV File Format

### Required Headers (in any order)

The CSV files must contain these exact header names:

```
ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference
```

### Header Descriptions

| Header | Description | Format | Example |
|--------|-------------|--------|---------|
| ProfileName | Account profile name | String | Card Campaign |
| TransactionDate | Transaction timestamp | yyyy-MM-dd HH:mm:ss | 2014-01-11 22:27:44 |
| TransactionAmount | Amount (negative for debits) | Decimal | -20000.0 |
| TransactionNarrative | Transaction description | String | *MOLEPS ATM25 MOLEPOLOLE BW |
| TransactionDescription | Transaction type description | String | DEDUCT |
| TransactionID | Unique transaction identifier | String | 0584011808649511 |
| TransactionType | Transaction category | 0 or 1 | 1 |
| WalletReference | Wallet reference code | String | P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5 |

### Sample CSV Data

```csv
ProfileName,TransactionDate,TransactionAmount,TransactionNarrative,TransactionDescription,TransactionID,TransactionType,WalletReference
Card Campaign,2014-01-11 22:27:44,-20000,*MOLEPS ATM25 MOLEPOLOLE BW,DEDUCT,0584011808649511,1,P_NzI2ODY2ODlfMTM4MjcwMTU2NS45MzA5
Card Campaign,2014-01-11 22:39:11,-10000,*MOGODITSHANE2 MOGODITHSANE BW,DEDUCT,0584011815513406,1,P_NzI1MjA1NjZfMTM3ODczODI3Mi4wNzY5
```

### Notes

- Empty fields are allowed (will be treated as null)
- Duplicate transactions (same TransactionID) are supported
- File must have at least a header row
- Lines with incorrect column counts will be rejected

## Configuration

### Application Properties

Create or modify `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=20MB

# Logging Configuration
logging.level.luka.mugosa.filecomparison=INFO
logging.level.org.springframework=WARN

# Virtual Threads (Java 21+)
spring.threads.virtual.enabled=true
```

### Docker-Specific Configuration

Create `src/main/resources/application-docker.properties` for Docker environment:

```properties
# Server Configuration
server.port=8080
server.address=0.0.0.0

# File Upload Configuration
spring.servlet.multipart.max-file-size=${MAX_FILE_SIZE:10MB}
spring.servlet.multipart.max-request-size=${MAX_REQUEST_SIZE:20MB}

# Logging Configuration
logging.level.root=${LOGGING_LEVEL_ROOT:INFO}
logging.level.luka.mugosa.filecomparison=${LOGGING_LEVEL_APP:INFO}
logging.file.name=/app/logs/application.log
logging.file.max-size=10MB
logging.file.max-history=7

# Actuator endpoints for health checks
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
management.health.defaults.enabled=true

# Virtual Threads
spring.threads.virtual.enabled=true
```

### Environment Variables for Docker

You can configure the application using environment variables in Docker:

| Variable | Description | Default |
|----------|-------------|---------|
| SPRING_PROFILES_ACTIVE | Active Spring profile | docker |
| MAX_FILE_SIZE | Maximum file upload size | 10MB |
| MAX_REQUEST_SIZE | Maximum request size | 20MB |
| LOGGING_LEVEL_ROOT | Root logging level | INFO |
| LOGGING_LEVEL_APP | Application logging level | INFO |
| JAVA_OPTS | JVM options | See Dockerfile |

### Adjusting Memory

**Local execution:**
```bash
java -Xmx2G -jar target/file-comparison-1.0.0.jar
```

**Docker execution:**
```bash
docker run -p 8080:8080 -m 2g file-reconciliation-app:latest
```

**Docker Compose:**
```yaml
deploy:
  resources:
    limits:
      memory: 2G
```

### Custom Scoring Weights

Modify `ScoringWeights.java` to adjust matching criteria:

```java
public class ScoringWeights {
    public static final double TRANSACTION_ID_WEIGHT = 40.0;
    public static final double AMOUNT_EXACT_WEIGHT = 25.0;
    public static final double DATE_EXACT_WEIGHT = 15.0;
    // ... adjust as needed
}
```

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ComparisonServiceImplTest
```

### Run with Coverage Report

```bash
mvn clean test jacoco:report
```

Coverage report will be available at: `target/site/jacoco/index.html`

### Test Files

Sample test CSV files are located in `src/test/resources/`:
- `test-file-1.csv`
- `test-file-2.csv`

### Testing Docker Image

**Test the Docker build:**
```bash
docker build -t file-reconciliation-app:test .
```

**Run integration tests in Docker:**
```bash
docker run --rm file-reconciliation-app:test mvn test
```

**Test health endpoint:**
```bash
docker run -d -p 8080:8080 --name test-app file-reconciliation-app:latest
sleep 10
curl http://localhost:8080/actuator/health
docker stop test-app && docker rm test-app
```

## Troubleshooting

### Common Issues

#### 1. Application Won't Start

**Error:** `Port 8080 is already in use`

**Solution (Local):**
```bash
# Change port
java -jar target/file-comparison-1.0.0.jar --server.port=8090
```

**Solution (Docker):**
```bash
# Map to different host port
docker run -p 9090:8080 file-reconciliation-app:latest
```

#### 2. File Size Exceeded

**Error:** `File size should be less than 10MB`

**Solution:**
- Split large CSV files into smaller chunks
- Or increase limit in `application.properties`:
```properties
spring.servlet.multipart.max-file-size=50MB
```

**Solution (Docker):**
```bash
docker run -p 8080:8080 -e MAX_FILE_SIZE=50MB file-reconciliation-app:latest
```

#### 3. Missing Headers

**Error:** `Missing required header: TransactionDate`

**Solution:** Ensure CSV file has exact header names (case-sensitive)

#### 4. Out of Memory

**Error:** `java.lang.OutOfMemoryError: Java heap space`

**Solution (Local):**
```bash
java -Xmx2G -jar target/file-comparison-1.0.0.jar
```

**Solution (Docker):**
```bash
docker run -p 8080:8080 -m 2g file-reconciliation-app:latest
```

#### 5. Parsing Timeout

**Error:** `File parsing operation timed out`

**Solution:** The application has a 2-minute timeout for file parsing. For very large files:
- Ensure files are not corrupted
- Check system resources
- Consider splitting files

#### 6. Docker Build Failures

**Error:** `Cannot connect to Maven repository`

**Solution:**
```bash
# Build with host network
docker build --network host -t file-reconciliation-app:latest .
```

**Error:** `Docker daemon not running`

**Solution:**
```bash
# Start Docker daemon (Linux)
sudo systemctl start docker

# Or use Docker Desktop (Mac/Windows)
```

#### 7. Container Won't Start

**Check container logs:**
```bash
docker logs file-reconciliation
```

**Common issues:**
- Port already in use: Change port mapping
- Permission denied: Check file permissions
- Out of memory: Increase container memory limit

#### 8. Health Check Failing

**Error:** Docker health check reports unhealthy

**Solution:**
```bash
# Check application logs
docker logs file-reconciliation

# Manually test health endpoint
docker exec file-reconciliation wget -O- http://localhost:8080/actuator/health

# Increase health check timeout in docker-compose.yml
healthcheck:
  timeout: 30s
  start_period: 60s
```

### Logging

**Enable debug logging locally:**

```properties
logging.level.luka.mugosa.filecomparison=DEBUG
```

**Enable debug logging in Docker:**

```bash
docker run -p 8080:8080 -e LOGGING_LEVEL_APP=DEBUG file-reconciliation-app:latest
```

**View logs from Docker container:**

```bash
# Follow logs
docker logs -f file-reconciliation

# View last 100 lines
docker logs --tail 100 file-reconciliation

# View logs with timestamps
docker logs -t file-reconciliation
```

**Configure file logging:**

```properties
logging.file.name=application.log
logging.file.path=/var/logs
```

**Access log files from Docker volume:**

```bash
# Run with volume mount
docker run -p 8080:8080 -v $(pwd)/logs:/app/logs file-reconciliation-app:latest

# Logs will be available in ./logs directory
tail -f logs/application.log
```

### Performance Optimization

**Docker performance tips:**

1. **Use multi-stage builds** (already implemented in Dockerfile)
2. **Limit resources appropriately:**
   ```bash
   docker run -p 8080:8080 --cpus="1.5" -m 1g file-reconciliation-app:latest
   ```
3. **Use volume caching for Maven dependencies:**
   ```bash
   docker run -v maven-repo:/root/.m2 file-reconciliation-app:latest
   ```
4. **Enable JVM container awareness** (already configured in Dockerfile)

## Production Deployment

### Deployment Checklist

- [ ] Configure appropriate memory limits
- [ ] Set production Spring profile
- [ ] Configure external logging
- [ ] Set up health monitoring
- [ ] Configure restart policies
- [ ] Use specific version tags (not `latest`)
- [ ] Set up backup and disaster recovery
- [ ] Configure SSL/TLS if exposed publicly
- [ ] Set up reverse proxy (nginx, traefik)
- [ ] Configure resource limits in orchestrator

### Example Production Deployment

**Docker Swarm:**
```bash
docker service create \
  --name file-reconciliation \
  --replicas 3 \
  --publish 8080:8080 \
  --limit-memory 1g \
  --reserve-memory 512m \
  file-reconciliation-app:1.0.0
```

**Kubernetes:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: file-reconciliation
spec:
  replicas: 3
  selector:
    matchLabels:
      app: file-reconciliation
  template:
    metadata:
      labels:
        app: file-reconciliation
    spec:
      containers:
      - name: file-reconciliation
        image: file-reconciliation-app:1.0.0
        ports:
        - containerPort: 8080
        resources:
          limits:
            memory: "1Gi"
            cpu: "1000m"
          requests:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 40
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 10
```

---