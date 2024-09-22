# Multi-cloud Manager

## Description
Multi-cloud Manager is a microservice-based application that enables users to manage virtual machines (VMs) across multiple cloud providers, including AWS, GCP, and Azure. Users can easily add their cloud accounts and manage their VMs from a single interface.

## Installation Instructions

### Prerequisites
- Docker and Docker Compose installed on your machine.

### Getting Started
1. Clone the repository:
   ```bash
   git clone 
   cd multi-cloud-manager
   ```

2. Set up environment variables for each service by creating `.env` files:
   - **Auth Service `.env` file**:
     ```
     SPRING_DATASOURCE_URL=jdbc:url
     SPRING_DATASOURCE_USERNAME=username
     SPRING_DATASOURCE_PASSWORD=password
     JWT_SECRET_KEY=jwt_key
     SUPPORT_EMAIL=user@mail.com
     APP_PASSWORD=password
     ```

   - **API Gateway `.env` file**:
     ```
     JWT_SECRET_KEY=key
     ```

   - **VM Service `.env` file**:
     ```
     ENCRYPTION_KEY=key
     DB_HOST=hostname
     DB_USER=username
     DB_PASSWORD=password
     DB_NAME=dbname
     KAFKA_BOOTSTRAP_SERVERS=kafka:9093
     KAFKA_TOPIC=user-registration
     SECURITY_JWT_SECRET_KEY=key
     AWS_ACCESS_KEY_ID=key_id
     AWS_SECRET_ACCESS_KEY=key_secreat
     AWS_DEFAULT_REGION=region
     ```

3. Start the application using Docker Compose:
   ```bash
   docker-compose up
   ```

## Usage Instructions
Once the application is running, users can interact with the UI to add their cloud accounts and manage their VMs.

## Performance Results

### Non-Cloud Setup
- **HTTP Request Performance**:
  ```
  Label           # Samples  Average  Min  Max   Std. Dev.  Error %  Throughput  Received KB/sec  Sent KB/sec  Avg. Bytes
  HTTP Request    300       1146     4    4108  1400.85     52.00%   66.44518   84.54            18.36        1302.8
  TOTAL           300       1146     4    4108  1400.85     52.00%   66.44518   84.54            18.36        1302.8
  ```

### Docker/Cloud-Based Setup
- **HTTP Request Performance**:
  ```
  Label           # Samples  Average  Min  Max   Std. Dev.  Error %  Throughput  Received KB/sec  Sent KB/sec  Avg. Bytes
  HTTP Request    300       5876     2207 12038 2502.82     0.00%    23.70043   9.67             6.76         418
  TOTAL           300       5876     2207 12038 2502.82     0.00%    23.70043   9.67             6.76         418
  ```
