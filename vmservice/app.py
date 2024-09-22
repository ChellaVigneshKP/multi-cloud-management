import signal
import sys
from flask import Flask
from prometheus_flask_exporter import PrometheusMetrics
from eureka import init_eureka
from kafka_consumer import start_kafka_consumer
from routes import setup_routes
from db import create_tables
app = Flask(__name__)

# Initialize Eureka
init_eureka(app)
metrics = PrometheusMetrics(app)


# Start Kafka consumer
start_kafka_consumer()

create_tables()
# Setup routes
setup_routes(app)


def cleanup(signal, frame):
    print("Cleaning up before exiting...")
    sys.exit(0)


if __name__ == '__main__':
    signal.signal(signal.SIGINT, cleanup)
    app.run(host='0.0.0.0', port=5000)
