import boto3
from db import get_aws_credentials
from config import AWS_REGIONS
import logging


def get_ec2_client(region):
    credentials = get_aws_credentials()
    if not credentials:
        raise Exception("No AWS credentials found in the database")

    return boto3.client(
        'ec2',
        aws_access_key_id=credentials['access_key_id'],
        aws_secret_access_key=credentials['secret_access_key'],
        region_name=region
    )


def list_instances_all_regions():
    all_instances = []

    for region in AWS_REGIONS:
        try:
            ec2 = get_ec2_client(region)
            response = ec2.describe_instances()
            for reservation in response['Reservations']:
                for instance in reservation['Instances']:
                    instance_info = {
                        'InstanceId': instance['InstanceId'],
                        'InstanceType': instance['InstanceType'],
                        'State': instance['State']['Name'],
                        'Region': region,
                        'PrivateIpAddress': instance.get('PrivateIpAddress', 'N/A'),
                        'PublicIpAddress': instance.get('PublicIpAddress', 'N/A'),
                    }
                    all_instances.append(instance_info)
        except Exception as e:
            logging.error(f"Error retrieving instances from region {region}: {e}")

    return all_instances
