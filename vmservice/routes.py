import logging
import os

from botocore.config import Config
from flask import jsonify, request
from dotenv import load_dotenv
from aws_account import validate_aws_credentials
from aws_client import list_instances_all_regions, get_ec2_client
from db import get_aws_credentials, encrypt_and_store_aws_credentials, get_user_id_from_username, \
    get_username_from_token, get_cloud_accounts_by_user, aws_account_exists, decrypt_aws_credentials, \
    get_all_cloud_accounts_by_user
import boto3

load_dotenv()
# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

AWS_ACCESS_KEY_ID = os.getenv('AWS_ACCESS_KEY_ID')
AWS_SECRET_ACCESS_KEY = os.getenv('AWS_SECRET_ACCESS_KEY')
AWS_DEFAULT_REGION = os.getenv('AWS_DEFAULT_REGION', 'us-east-1')

session = boto3.Session()
config = Config(max_pool_connections=50)
ec2_client = session.client(
    'ec2',
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_DEFAULT_REGION,
    config=config
)


def setup_routes(app):
    @app.route('/vm/ec2', methods=['GET'])
    def list_ec2_instances():
        try:
            ec2 = get_ec2_client(get_aws_credentials()['region'])
            response = ec2.describe_instances()
            instances = []
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
            return jsonify({'instances': instances}), 200
        except Exception as e:
            logger.error("Error retrieving EC2 instances", exc_info=e)
            return jsonify({'error': str(e)}), 500

    @app.route('/vm/all', methods=['GET'])
    def list_all_instances():
        try:
            instances = list_instances_all_regions()
            return jsonify({'instances': instances}), 200
        except Exception as e:
            logger.error("Error retrieving all instances", exc_info=e)
            return jsonify({'error': str(e)}), 500

    @app.route('/vm/aws/regions', methods=['GET'])
    def list_aws_regions():
        try:
            response = ec2_client.describe_regions()
            regions = [region['RegionName'] for region in response['Regions']]
            return jsonify({'regions': regions}), 200
        except Exception as e:
            logger.error("Error retrieving AWS regions", exc_info=e)
            return jsonify({'error': "Error retrieving regions"}), 500

    @app.route('/vm/aws/addaccount', methods=['POST'])
    def add_aws_account():
        data = request.json
        username = get_username_from_token(request.headers['Authorization'])
        logger.info(f"Username from Token: {username}")  # Implement this function
        user_id = get_user_id_from_username(username)
        logger.info(f"UserId with Username: {user_id}")

        access_key_id = data['access_key_id']
        secret_access_key = data['secret_access_key']
        region = data['region']
        if aws_account_exists(user_id, access_key_id):
            return jsonify({"message": "Account already added for this user"}), 400

        # Validate AWS credentials
        if validate_aws_credentials(access_key_id, secret_access_key):
            # Encrypt and store credentials
            encrypt_and_store_aws_credentials(user_id, access_key_id, secret_access_key, region)
            return jsonify({"message": "AWS account added successfully"}), 201
        else:
            return jsonify({"message": "Invalid AWS credentials"}), 400

    @app.route('/vm/user', methods=['GET'])
    def get_user_name():
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({"message": "Token is missing!"}), 401

        username = get_username_from_token(token)
        if username is None:
            logger.info("Username from Token: None")
            return jsonify({"message": "Invalid or expired token!"}), 401

        logger.info(f"Username from Token: {username}")
        user_id = get_user_id_from_username(username)
        logger.info(f"UserId with Username: {user_id}")

        return jsonify({"username": username, "user_id": user_id}), 200

    @app.route('/vm/cloudaccounts', methods=['GET'])
    def get_cloud_accounts():
        token = request.headers.get('Authorization')

        # Get the username from the token
        username = get_username_from_token(token)
        if username is None:
            return jsonify({"message": "Invalid or expired token!"}), 401

        # Get the user ID for the authenticated user
        user_id = get_user_id_from_username(username)
        if not user_id:
            return jsonify({"message": "User not found!"}), 404

        # Fetch cloud accounts for the user
        accounts = get_cloud_accounts_by_user(user_id)
        if accounts is None:
            return jsonify({"message": "Error fetching cloud accounts"}), 500

        return jsonify(accounts), 200

    @app.route('/vm/aws/listvms', methods=['GET'])
    def list_vms():
        token = request.headers.get('Authorization')
        username = get_username_from_token(token)
        user_id = get_user_id_from_username(username)

        # Get AWS accounts for the user
        accounts = get_all_cloud_accounts_by_user(user_id)
        if accounts is None:
            return jsonify({"message": "Error fetching AWS accounts"}), 500

        all_instances = []

        for account in accounts:
            access_key_id = account['access_key_id']
            secret_access_key = account['secret_access_key']
            region = account['region']

            # Format keyId to show only first 4 and last 4 characters
            formatted_key_id = f"{access_key_id[:4]}XXXX{access_key_id[-4:]}"

            try:
                # Create an EC2 client with decrypted credentials
                ec2_client = boto3.client('ec2',
                                          aws_access_key_id=access_key_id,
                                          aws_secret_access_key=secret_access_key,
                                          region_name=region
                                          )
                response = ec2_client.describe_instances()

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
                            # Default to 'Linux' if platform info is not available
                            'state': instance['State']['Name'],
                        }
                        all_instances.append(instance_info)

            except Exception as e:
                logger.error(f"Error retrieving instances for account: {account['access_key_id']}, {e}")

        return jsonify({'instances': all_instances}), 200
