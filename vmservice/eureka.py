import py_eureka_client.eureka_client as eureka_client
import logging


def init_eureka(app):
    eureka_client.init(eureka_server="http://localhost:8761/eureka", app_name="vm-service",
                       instance_port=5000, instance_host="localhost")
    logging.info("Eureka initialized.")


def cleanup_eureka():
    eureka_client.stop()
    logging.info("Service deregistered from Eureka.")
