from kafka import KafkaConsumer
import json
import threading
from config import KAFKA_CONFIG
import logging
from db import add_user_to_db


def consume_kafka_messages():
    consumer = KafkaConsumer(
        KAFKA_CONFIG['topic'],
        bootstrap_servers=KAFKA_CONFIG['bootstrap_servers'],
        auto_offset_reset='earliest',
        enable_auto_commit=True,
        group_id='python-consumer-group',
        value_deserializer=lambda x: json.loads(x.decode('utf-8'))
    )

    for message in consumer:
        logging.info(f"Received message: {message.value}")
        user_data = message.value
        add_user_to_db(user_data['username'], user_data['email'])


def start_kafka_consumer():
    thread = threading.Thread(target=consume_kafka_messages)
    thread.daemon = True
    thread.start()
