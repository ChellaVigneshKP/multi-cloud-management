import logging

import boto3
from botocore.exceptions import NoCredentialsError, PartialCredentialsError, ClientError

# Configure logging to display informational messages
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# Function to validate AWS credentials by attempting to call the STS (Security Token Service)
def validate_aws_credentials(access_key_id, secret_access_key):
    try:
        # Create an STS client using the provided AWS access key and secret key
        sts_client = boto3.client(
            'sts',
            aws_access_key_id=access_key_id,
            aws_secret_access_key=secret_access_key,
        )

        # Attempt to get the identity of the AWS account associated with these credentials
        # This serves as a way to verify that the credentials are valid
        sts_client.get_caller_identity()

        # If the credentials are valid, return True
        return True
    except (NoCredentialsError, PartialCredentialsError, ClientError) as e:
        # If there's any error related to credentials, log the error and return False
        logger.error(f"Error validating AWS credentials: {e}")
        return False
