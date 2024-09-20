import logging

import boto3
from botocore.exceptions import NoCredentialsError, PartialCredentialsError, ClientError

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def validate_aws_credentials(access_key_id, secret_access_key):
    try:
        sts_client = boto3.client(
            'sts',
            aws_access_key_id=access_key_id,
            aws_secret_access_key=secret_access_key,
        )
        sts_client.get_caller_identity()
        return True
    except (NoCredentialsError, PartialCredentialsError, ClientError) as e:
        logger.error(f"Error validating AWS credentials: {e}")
        return False
