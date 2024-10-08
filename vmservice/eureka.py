import py_eureka_client.eureka_client as eureka_client
import logging

def init_eureka(app):
    """
    Initialize the Eureka client and register the service with the Eureka server.

    Args:
        app: The Flask application instance to which this service belongs.
    """
    # Initialize Eureka client with server URL, application name, and instance details
    eureka_client.init(eureka_server="http://localhost:8761/eureka",
                       app_name="VM-SERVICE",
                       instance_port=5000,
                       instance_host="localhost")
    logging.info("Eureka initialized.")  # Log successful initialization

def cleanup_eureka():
    """
    Deregister the service from the Eureka server during cleanup.

    This function should be called when the application is shutting down to
    ensure the service is properly deregistered from the Eureka service discovery.
    """
    eureka_client.stop()  # Stop the Eureka client
    logging.info("Service deregistered from Eureka.")  # Log successful deregistration
