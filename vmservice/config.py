from dotenv import load_dotenv
import os

load_dotenv()

DATABASE_CONFIG = {
    'host': os.getenv('DB_HOST'),
    'user': os.getenv('DB_USER'),
    'password': os.getenv('DB_PASSWORD'),
    'database': os.getenv('DB_NAME')
}

KAFKA_CONFIG = {
    'bootstrap_servers': os.getenv('KAFKA_BOOTSTRAP_SERVERS'),
    'topic': os.getenv('KAFKA_TOPIC')
}


AWS_REGIONS = ['us-east-1', 'us-west-1', 'us-west-2', 'eu-central-1', 'ap-south-1']
