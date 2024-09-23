import boto3
from db import get_aws_credentials
from config import AWS_REGIONS
import logging


# Function to get an EC2 client for a specific AWS region
def get_ec2_client(region):
    # Fetch AWS credentials from the database
    credentials = get_aws_credentials()

    # Raise an exception if no credentials are found
    if not credentials:
        raise Exception("No AWS credentials found in the database")

    # Create and return a boto3 EC2 client using the retrieved credentials
    return boto3.client(
        'ec2',
        aws_access_key_id=credentials['access_key_id'],
        aws_secret_access_key=credentials['secret_access_key'],
        region_name=region
    )


# Function to list all EC2 instances across all regions
def list_instances_all_regions():
    all_instances = []  # List to store instances across regions

    # Loop through all regions specified in the AWS_REGIONS config
    for region in AWS_REGIONS:
        try:
            # Get EC2 client for the current region
            ec2 = get_ec2_client(region)

            # Call EC2 API to describe instances in the current region
            response = ec2.describe_instances()

            # Loop through reservations to extract instance information
            for reservation in response['Reservations']:
                for instance in reservation['Instances']:
                    # Build a dictionary with instance details
                    instance_info = {
                        'InstanceId': instance['InstanceId'],
                        'InstanceType': instance['InstanceType'],
                        'State': instance['State']['Name'],
                        'Region': region,
                        'PrivateIpAddress': instance.get('PrivateIpAddress', 'N/A'),
                        'PublicIpAddress': instance.get('PublicIpAddress', 'N/A'),
                    }
                    # Append the instance info to the list of all instances
                    all_instances.append(instance_info)

        # Log an error if any issue occurs while retrieving instances from the region
        except Exception as e:
            logging.error(f"Error retrieving instances from region {region}: {e}")

    # Return the complete list of instances from all regions
    return all_instances
