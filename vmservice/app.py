import signal
import sys
from flask import Flask
from prometheus_flask_exporter import PrometheusMetrics
from eureka import init_eureka
from kafka_consumer import start_kafka_consumer
from routes import setup_routes
from db import create_tables

# Initialize the Flask application
app = Flask(__name__)

# Initialize Eureka for service discovery
init_eureka(app)

# Initialize Prometheus metrics for monitoring and exporting application metrics
metrics = PrometheusMetrics(app)

# Start the Kafka consumer to listen for messages from the Kafka topic
start_kafka_consumer()

# Create database tables if they don't already exist
create_tables()

# Set up the routes for the Flask app
setup_routes(app)


# Define a cleanup function that is triggered when the application is terminated
def cleanup(signal, frame):
    print("Cleaning up before exiting...")
    sys.exit(0)  # Exit the program cleanly


# Main entry point to start the Flask app and handle SIGINT (Ctrl+C) signal for graceful shutdown
if __name__ == '__main__':
    # Register a signal handler for SIGINT (Ctrl+C) to ensure proper cleanup
    signal.signal(signal.SIGINT, cleanup)

    # Run the Flask app, listening on all available IP addresses on port 5000
    app.run(host='0.0.0.0', port=5000)
