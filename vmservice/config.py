from dotenv import load_dotenv
import os

# Load environment variables from the .env file
load_dotenv()

# Configuration dictionary for the database connection.
# It retrieves database connection details from the environment variables set in the .env file.
DATABASE_CONFIG = {
    'host': os.getenv('DB_HOST'),         # Database host (e.g., localhost or remote host)
    'user': os.getenv('DB_USER'),         # Username for the database
    'password': os.getenv('DB_PASSWORD'), # Password for the database
    'database': os.getenv('DB_NAME')      # Name of the database to connect to
}

# Configuration dictionary for Kafka setup.
# It retrieves Kafka-related configurations from the environment variables set in the .env file.
KAFKA_CONFIG = {
    'bootstrap_servers': os.getenv('KAFKA_BOOTSTRAP_SERVERS'), # Kafka server address (e.g., kafka:9092)
    'topic': os.getenv('KAFKA_TOPIC')                          # Kafka topic to be used (e.g., user-registration)
}