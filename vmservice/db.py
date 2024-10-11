import base64
import logging
import os
import jwt
import psycopg2
from cryptography.fernet import Fernet
from dotenv import load_dotenv
from psycopg2 import sql

from config import DATABASE_CONFIG

# Load environment variables
load_dotenv()

# Load encryption key for Fernet encryption and decode the JWT secret key
ENCRYPTION_KEY = os.getenv('ENCRYPTION_KEY').encode()  # Store the encryption key securely
cipher = Fernet(ENCRYPTION_KEY)  # Initialize cipher for encryption/decryption using Fernet
encoded_key = os.getenv('SECURITY_JWT_SECRET_KEY')
SECRET_KEY = base64.b64decode(encoded_key)  # Decode the base64-encoded JWT secret key

# Setup logging configuration for the module
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

AWS_REGIONS = [
    ("ap-south-1", "Asia Pacific (Mumbai)"),
    ("eu-north-1", "EU (Stockholm)"),
    ("eu-west-3", "EU (Paris)"),
    ("eu-west-2", "EU (London)"),
    ("eu-west-1", "EU (Ireland)"),
    ("ap-northeast-3", "Asia Pacific (Osaka-Local)"),
    ("ap-northeast-2", "Asia Pacific (Seoul)"),
    ("ap-northeast-1", "Asia Pacific (Tokyo)"),
    ("ca-central-1", "Canada (Central)"),
    ("sa-east-1", "South America (Sao Paulo)"),
    ("ap-southeast-1", "Asia Pacific (Singapore)"),
    ("ap-southeast-2", "Asia Pacific (Sydney)"),
    ("eu-central-1", "EU (Frankfurt)"),
    ("us-east-1", "US East (N. Virginia)"),
    ("us-east-2", "US East (Ohio)"),
    ("us-west-1", "US West (N. California)"),
    ("us-west-2", "US West (Oregon)")
]

def create_tables():
    """
    Function to create the necessary database tables if they do not exist.
    The tables include users, aws_cloud_accounts, gcp_accounts, azure_accounts, and aws_regions.
    """
    connection = None
    try:
        connection = get_db_connection()
        cursor = connection.cursor()

        # List of SQL queries to create the required tables
        table_creation_queries = [
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id SERIAL PRIMARY KEY,
                username VARCHAR(255) NOT NULL UNIQUE,
                email VARCHAR(255) NOT NULL UNIQUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS aws_cloud_accounts (
                id SERIAL PRIMARY KEY,
                user_id INT NOT NULL,
                access_key_id VARCHAR(255) NOT NULL,
                secret_access_key VARCHAR(255) NOT NULL,
                region VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS gcp_accounts (
                id SERIAL PRIMARY KEY,
                user_id INT NOT NULL,
                project_id VARCHAR(255) NOT NULL,
                credentials JSONB NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS azure_accounts (
                id SERIAL PRIMARY KEY,
                user_id INT NOT NULL,
                client_id VARCHAR(255) NOT NULL,
                client_secret VARCHAR(255) NOT NULL,
                tenant_id VARCHAR(255) NOT NULL,
                subscription_id VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS aws_regions (
                region_id SERIAL PRIMARY KEY,
                region_name VARCHAR(100) UNIQUE NOT NULL,
                region_description VARCHAR(255)
            );
            """
        ]

        # Execute all table creation queries
        for query in table_creation_queries:
            cursor.execute(query)

        # Commit the transaction to apply changes
        connection.commit()
        logging.info("Tables created successfully.")

        # Initialize AWS regions after creating the tables
        initialize_aws_regions(connection)

    except Exception as e:
        logging.error(f"Error creating tables: {e}")
    finally:
        if cursor:
            cursor.close()
        if connection:
            connection.close()

def get_db_connection():
    """
    Establish a connection to the PostgreSQL database using the configuration
    from the DATABASE_CONFIG dictionary.
    """
    return psycopg2.connect(
        host=DATABASE_CONFIG['host'],
        user=DATABASE_CONFIG['user'],
        password=DATABASE_CONFIG['password'],
        database=DATABASE_CONFIG['database']
    )

def initialize_aws_regions(connection):
    """
    Function to initialize AWS regions in the `aws_regions` table.
    """
    try:
        cursor = connection.cursor()

        # Insert regions into the aws_regions table if they don't already exist
        insert_query = """
        INSERT INTO aws_regions (region_name, region_description)
        VALUES (%s, %s) ON CONFLICT (region_name) DO NOTHING;
        """
        cursor.executemany(insert_query, AWS_REGIONS)

        # Commit the transaction
        connection.commit()
        logger.info("AWS regions initialized successfully.")
    except Exception as e:
        logging.error(f"Error initializing AWS regions: {e}")
    finally:
        if cursor:
            cursor.close()


def get_aws_credentials():
    """
    Retrieve AWS credentials (access_key_id, secret_access_key, and region)
    from the aws_cloud_accounts table.
    """
    connection = None
    try:
        connection = get_db_connection()
        with connection.cursor() as cursor:
            cursor.execute("SELECT access_key_id, secret_access_key, region FROM aws_accounts LIMIT 1")
            result = cursor.fetchone()
            if result:
                logging.info("Successfully retrieved AWS credentials")
                return {
                    'access_key_id': result[0],
                    'secret_access_key': result[1],
                    'region': result[2]
                }
            return None
    except Exception as e:
        logging.error(f"Error retrieving AWS credentials: {e}")
        return None
    finally:
        if connection:
            connection.close()


def add_user_to_db(username, email):
    """
    Add a new user with the provided username and email to the users table.
    """
    connection = get_db_connection()
    try:
        with connection.cursor() as cursor:
            # Insert the new user into the users table
            cursor.execute(
                sql.SQL("INSERT INTO users (username, email) VALUES (%s, %s) RETURNING user_id"),
                (username, email)
            )
            user_id = cursor.fetchone()[0]  # Retrieve the new user's ID
            connection.commit()
            logging.info(f"User added with ID: {user_id}")
    except Exception as e:
        logging.error(f"Error adding user to DB: {e}")
        connection.rollback()  # Rollback in case of error
    finally:
        connection.close()


def encrypt_and_store_aws_credentials(user_id, access_key_id, secret_access_key, region):
    """
    Encrypt AWS credentials and store them in the aws_cloud_accounts table.
    """
    connection = get_db_connection()
    try:
        with connection.cursor() as cursor:
            # Encrypt the access key ID and secret access key using Fernet cipher
            encrypted_access_key_id = cipher.encrypt(access_key_id.encode()).decode()
            encrypted_secret_access_key = cipher.encrypt(secret_access_key.encode()).decode()

            # Insert the encrypted credentials into the aws_cloud_accounts table
            cursor.execute(
                sql.SQL(
                    "INSERT INTO aws_cloud_accounts (user_id, access_key_id, secret_access_key, region) VALUES (%s, %s, %s, %s)"),
                (user_id, encrypted_access_key_id, encrypted_secret_access_key, region)
            )
            connection.commit()
            logging.info("AWS credentials stored successfully")
    except Exception as e:
        logging.error(f"Error storing AWS credentials: {e}")
        connection.rollback()
    finally:
        connection.close()


def aws_account_exists(user_id, access_key_id):
    """
    Check if an AWS account with the provided access_key_id already exists for the given user.
    """
    connection = get_db_connection()
    try:
        with connection.cursor() as cursor:
            cursor.execute(
                sql.SQL("SELECT access_key_id FROM aws_cloud_accounts WHERE user_id = %s"),
                (user_id,)
            )
            rows = cursor.fetchall()

            for row in rows:
                # Decrypt each stored access_key_id for comparison
                decrypted_access_key_id = cipher.decrypt(row[0].encode()).decode()
                if decrypted_access_key_id == access_key_id:
                    logging.info(f"AWS account with user_id {user_id} exists.")
                    return True

            logging.info(f"No AWS account found for user_id {user_id} with the given access_key_id.")
            return False

    except Exception as e:
        logger.error(f"Error checking AWS account existence: {e}")
        return False
    finally:
        connection.close()


def get_username_from_token(token):
    """
    Extract the username (or user identifier) from the JWT token.
    """
    if not token:
        logging.error("No token provided")
        return None

    # Ensure token is in the format "Bearer <token>"
    if not token.startswith("Bearer "):
        logging.error("Invalid token format")
        return None
    try:
        # Extract the token part after "Bearer "
        token = token.split(" ")[1]

        # Decode the token using the SECRET_KEY and specified algorithm
        payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])

        # Optionally, log the fact that decoding was successful
        logging.info("Token decoded successfully")

        # Return the username or user identifier from the payload
        return payload.get('sub')  # Adjust according to your token structure

    except jwt.ExpiredSignatureError:
        logging.warning("Token has expired")
        return None
    except jwt.InvalidTokenError as e:
        logging.warning(f"Invalid token: {e}")
        return None
    except Exception as e:
        logging.error(f"An error occurred while decoding token: {e}")
        return None


def get_user_id_from_username(username):
    """
    Retrieve the user ID based on the username from the users table.
    """
    connection = get_db_connection()
    try:
        with connection.cursor() as cursor:
            cursor.execute("SELECT user_id FROM users WHERE username = %s", (username,))
            result = cursor.fetchone()
            return result[0] if result else None
    except Exception as e:
        logging.error(f"Error retrieving user ID: {e}")
        return None
    finally:
        connection.close()


def get_cloud_accounts_by_user(user_id):
    connection = None
    try:
        connection = get_db_connection()
        with connection.cursor() as cursor:
            # AWS Accounts - decrypt the access key ID and secret access key
            cursor.execute(
                "SELECT 'AWS' as cloud_name, access_key_id FROM aws_cloud_accounts WHERE user_id = %s",
                (user_id,)
            )
            aws_accounts = cursor.fetchall()

            # Decrypt AWS credentials
            decrypted_aws_accounts = []
            for row in aws_accounts:
                decrypted_access_key_id = cipher.decrypt(row[1].encode()).decode()
                decrypted_aws_accounts.append({
                    'cloud_name': row[0],
                    'access_key_id': decrypted_access_key_id,
                })

            # GCP Accounts
            cursor.execute(
                "SELECT 'GCP' as cloud_name, project_id FROM gcp_accounts WHERE user_id = %s",
                (user_id,)
            )
            gcp_accounts = cursor.fetchall()

            # Azure Accounts
            cursor.execute(
                "SELECT 'Azure' as cloud_name, client_id FROM azure_accounts WHERE user_id = %s",
                (user_id,)
            )
            azure_accounts = cursor.fetchall()

            # Combine all accounts into a single list
            all_accounts = decrypted_aws_accounts + [{'cloud_name': row[0], 'project_id': row[1]} for row in
                                                     gcp_accounts] \
                           + [{'cloud_name': row[0], 'client_id': row[1]} for row in azure_accounts]

            return all_accounts

    except Exception as e:
        logging.error(f"Error fetching cloud accounts: {e}")
        return None
    finally:
        if connection:
            connection.close()


def decrypt_aws_credentials(encrypted_access_key_id, encrypted_secret_access_key):
    """
    Decrypts the provided AWS credentials using the Fernet cipher.
    """
    try:
        decrypted_access_key_id = cipher.decrypt(encrypted_access_key_id.encode()).decode()
        decrypted_secret_access_key = cipher.decrypt(encrypted_secret_access_key.encode()).decode()
        return decrypted_access_key_id, decrypted_secret_access_key
    except Exception as e:
        logging.error(f"Error decrypting AWS credentials: {e}")
        return None, None


def get_all_cloud_accounts_by_user(user_id):
    connection = None
    try:
        connection = get_db_connection()
        with connection.cursor() as cursor:
            # Fetch AWS Accounts
            cursor.execute(
                "SELECT access_key_id, secret_access_key, region FROM aws_cloud_accounts WHERE user_id = %s",
                (user_id,)
            )
            aws_accounts = cursor.fetchall()

            # Decrypt AWS credentials
            decrypted_aws_accounts = []
            for row in aws_accounts:
                decrypted_access_key_id = cipher.decrypt(row[0].encode()).decode()
                decrypted_secret_access_key = cipher.decrypt(row[1].encode()).decode()
                region = row[2]

                decrypted_aws_accounts.append({
                    'access_key_id': decrypted_access_key_id,
                    'secret_access_key': decrypted_secret_access_key,
                    'region': region
                })

            return decrypted_aws_accounts

    except Exception as e:
        logging.error(f"Error fetching AWS accounts: {e}")
        return None
    finally:
        if connection:
            connection.close()
