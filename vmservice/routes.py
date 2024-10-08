import logging
import os

from botocore.config import Config
from flask import jsonify, request
from dotenv import load_dotenv
from aws_account import validate_aws_credentials
from aws_client import list_instances_all_regions, get_ec2_client
from db import get_aws_credentials, encrypt_and_store_aws_credentials, get_user_id_from_username, \
    get_cloud_accounts_by_user, aws_account_exists, decrypt_aws_credentials, \
    get_all_cloud_accounts_by_user
import boto3

# Load environment variables from the .env file
load_dotenv()

# Configure logging for the application
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Fetch AWS credentials and default region from environment variables
AWS_ACCESS_KEY_ID = os.getenv('AWS_ACCESS_KEY_ID')
AWS_SECRET_ACCESS_KEY = os.getenv('AWS_SECRET_ACCESS_KEY')
AWS_DEFAULT_REGION = os.getenv('AWS_DEFAULT_REGION', 'us-east-1')

# Create a boto3 session and configure the EC2 client with a connection pool
session = boto3.Session()
config = Config(max_pool_connections=50)
ec2_client = session.client(
    'ec2',
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_DEFAULT_REGION,
    config=config
)


# Function to set up routes for the Flask app
def setup_routes(app):
    # Route to list EC2 instances in a specific AWS region
    @app.route('/vm/ec2', methods=['GET'])
    def list_ec2_instances():
        try:
            # Fetch the EC2 client for the region from the database-stored credentials
            ec2 = get_ec2_client(get_aws_credentials()['region'])
            response = ec2.describe_instances()  # Call to AWS to describe instances
            instances = []

            # Extract details of each instance from the response
            for reservation in response['Reservations']:
                for instance in reservation['Instances']:
                    instance_info = {
                        'InstanceId': instance['InstanceId'],
                        'InstanceType': instance['InstanceType'],
                        'State': instance['State']['Name'],
                        'PrivateIpAddress': instance.get('PrivateIpAddress', 'N/A'),
                        'PublicIpAddress': instance.get('PublicIpAddress', 'N/A'),
                    }
                    instances.append(instance_info)

            return jsonify({'instances': instances}), 200  # Return the instance data as JSON
        except Exception as e:
            logger.error("Error retrieving EC2 instances", exc_info=e)
            return jsonify({'error': str(e)}), 500  # Handle errors and return a 500 status code

    # Route to list EC2 instances across all AWS regions
    @app.route('/vm/all', methods=['GET'])
    def list_all_instances():
        try:
            instances = list_instances_all_regions()  # Fetch instances from all regions
            return jsonify({'instances': instances}), 200  # Return instance data as JSON
        except Exception as e:
            logger.error("Error retrieving all instances", exc_info=e)
            return jsonify({'error': str(e)}), 500  # Handle errors and return a 500 status code

    # Route to list all available AWS regions
    @app.route('/vm/aws/regions', methods=['GET'])
    def list_aws_regions():
        try:
            response = ec2_client.describe_regions()  # Call AWS to describe all available regions
            regions = [region['RegionName'] for region in response['Regions']]  # Extract region names
            return jsonify({'regions': regions}), 200  # Return region names as JSON
        except Exception as e:
            logger.error("Error retrieving AWS regions", exc_info=e)
            return jsonify({'error': "Error retrieving regions"}), 500  # Handle errors and return a 500 status code

    # Route to add a new AWS account for the user
    @app.route('/vm/aws/addaccount', methods=['POST'])
    def add_aws_account():
        data = request.json
        username = request.headers.get('X-User-Name')
        if not username:
            return jsonify({"message": "User not authenticated"}), 401
        logger.info(f"Username from Header: {username}")

        user_id = get_user_id_from_username(username)
        if not user_id:
            return jsonify({"message": "User not found"}), 404
        logger.info(f"UserId with Username: {user_id}")

        access_key_id = data['access_key_id']
        secret_access_key = data['secret_access_key']
        region = data['region']

        # Check if the AWS account already exists for the user
        if aws_account_exists(user_id, access_key_id):
            return jsonify({"message": "Account already added for this user"}), 400  # Return error if account exists

        # Validate AWS credentials
        if validate_aws_credentials(access_key_id, secret_access_key):
            # Encrypt and store AWS credentials in the database
            encrypt_and_store_aws_credentials(user_id, access_key_id, secret_access_key, region)
            return jsonify({"message": "AWS account added successfully"}), 201  # Return success message
        else:
            return jsonify({"message": "Invalid AWS credentials"}), 400  # Return error if credentials are invalid

    # Route to get the current user's username from the headers
    @app.route('/vm/user', methods=['GET'])
    def get_user_name():
        username = request.headers.get('X-User-Name')
        if not username:
            return jsonify({"message": "User not authenticated"}), 401
        logger.info(f"Username from Header: {username}")

        user_id = get_user_id_from_username(username)
        if not user_id:
            return jsonify({"message": "User not found!"}), 404
        logger.info(f"UserId with Username: {user_id}")

        return jsonify({"username": username, "user_id": user_id}), 200  # Return the username and user ID

    # Route to get all cloud accounts linked to the user
    @app.route('/vm/cloudaccounts', methods=['GET'])
    def get_cloud_accounts():
        username = request.headers.get('X-User-Name')
        if not username:
            return jsonify({"message": "User not authenticated"}), 401

        user_id = get_user_id_from_username(username)
        if not user_id:
            return jsonify({"message": "User not found!"}), 404

        accounts = get_cloud_accounts_by_user(user_id)
        if accounts is None:
            return jsonify({"message": "Error fetching cloud accounts"}), 500  # Handle errors

        return jsonify(accounts), 200  # Return cloud accounts as JSON

    # Route to list all VMs across multiple AWS accounts linked to the user
    @app.route('/vm/aws/listvms', methods=['GET'])
    def list_vms():
        username = request.headers.get('X-User-Name')
        if not username:
            return jsonify({"message": "User not authenticated"}), 401

        user_id = get_user_id_from_username(username)
        if not user_id:
            return jsonify({"message": "User not found!"}), 404

        accounts = get_all_cloud_accounts_by_user(user_id)
        if accounts is None:
            return jsonify({"message": "Error fetching AWS accounts"}), 500  # Handle errors

        all_instances = []  # Store details of all VMs

        for account in accounts:
            access_key_id = account['access_key_id']
            secret_access_key = account['secret_access_key']
            region = account['region']

            # Format keyId to show only first 4 and last 4 characters
            formatted_key_id = f"{access_key_id[:4]}XXXX{access_key_id[-4:]}"

            try:
                # Create an EC2 client using the decrypted credentials for the specific region
                ec2_client = boto3.client('ec2',
                                          aws_access_key_id=access_key_id,
                                          aws_secret_access_key=secret_access_key,
                                          region_name=region
                                          )
                response = ec2_client.describe_instances()  # Fetch instance details for the account

                # Extract instance information and add to the list
                for reservation in response['Reservations']:
                    for instance in reservation['Instances']:
                        instance_info = {
                            'provider': 'AWS',
                            'keyId': formatted_key_id,
                            'name': instance.get('Tags', [{'Key': 'Name', 'Value': 'N/A'}])[0].get('Value', 'N/A'),
                            'instanceId': instance['InstanceId'],
                            'type': instance['InstanceType'],
                            'zone': instance['Placement']['AvailabilityZone'],
                            'publicIPV4Dns': instance.get('PublicDnsName', 'N/A'),
                            'publicIPV4Address': instance.get('PublicIpAddress', 'N/A'),
                            'securityGroup': instance['SecurityGroups'][0]['GroupName'] if instance[
                                'SecurityGroups'] else 'N/A',
                            'platform': instance.get('Platform', 'Linux'),
                            'state': instance['State']['Name'],
                        }
                        all_instances.append(instance_info)  # Add instance details to the list

            except Exception as e:
                logger.error(f"Error retrieving instances for account: {account['access_key_id']}, {e}")  # Log errors

        return jsonify({'instances': all_instances}), 200  # Return the list of instances as JSON