from kafka import KafkaConsumer
import json
import threading
from config import KAFKA_CONFIG
import logging
from db import add_user_to_db

def consume_kafka_messages():
    """
    Consumes messages from a Kafka topic and processes each message.

    This function creates a Kafka consumer that listens for messages on the
    configured topic and adds user data to the database upon receiving messages.
    """
    # Initialize the Kafka consumer with the specified topic and configurations
    consumer = KafkaConsumer(
        KAFKA_CONFIG['topic'],
        bootstrap_servers=KAFKA_CONFIG['bootstrap_servers'],
        auto_offset_reset='earliest',  # Start reading at the earliest message
        enable_auto_commit=True,  # Automatically commit offsets
        group_id='python-consumer-group',  # Consumer group ID
        value_deserializer=lambda x: json.loads(x.decode('utf-8'))  # Deserialize message value from JSON
    )

    # Continuously listen for messages on the Kafka topic
    for message in consumer:
        logging.info(f"Received message: {message.value}")  # Log the received message
        user_data = message.value  # Extract user data from the message
        add_user_to_db(user_data['username'], user_data['email'])  # Add user data to the database


def start_kafka_consumer():
    """
    Starts the Kafka consumer in a separate thread.

    This function creates a new thread to run the Kafka message consumption
    process, allowing the main application to continue running concurrently.
    """
    thread = threading.Thread(target=consume_kafka_messages)  # Create a thread for the consumer
    thread.daemon = True  # Set the thread as a daemon so it exits when the main program does
    thread.start()  # Start the thread
