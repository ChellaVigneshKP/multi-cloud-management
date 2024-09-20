import base64
import logging
import os
import jwt
import psycopg2
from cryptography.fernet import Fernet
from dotenv import load_dotenv
from psycopg2 import sql

from config import DATABASE_CONFIG

load_dotenv()
ENCRYPTION_KEY = os.getenv('ENCRYPTION_KEY').encode()  # Store this securely
cipher = Fernet(ENCRYPTION_KEY)
encoded_key = os.getenv('SECURITY_JWT_SECRET_KEY')
SECRET_KEY = base64.b64decode(encoded_key)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def create_tables():
    connection = None
    try:
        connection = get_db_connection()
        cursor = connection.cursor()

        # List of table creation queries
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
            """
        ]

        # Execute each table creation query
        for query in table_creation_queries:
            cursor.execute(query)

        # Commit the changes
        connection.commit()
        logging.info("Tables created successfully.")

    except Exception as e:
        logging.error(f"Error creating tables: {e}")
    finally:
        if cursor:
            cursor.close()
        if connection:
            connection.close()


def get_db_connection():
    return psycopg2.connect(
        host=DATABASE_CONFIG['host'],
        user=DATABASE_CONFIG['user'],
        password=DATABASE_CONFIG['password'],
        database=DATABASE_CONFIG['database']
    )


def get_aws_credentials():
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
    connection = get_db_connection()
    try:
        with connection.cursor() as cursor:
            # Insert user into the User table
            cursor.execute(
                sql.SQL("INSERT INTO users (username, email) VALUES (%s, %s) RETURNING user_id"),
                (username, email)
            )
            user_id = cursor.fetchone()[0]
            connection.commit()
            logging.info(f"User added with ID: {user_id}")
    except Exception as e:
        logging.error(f"Error adding user to DB: {e}")
        connection.rollback()
    finally:
        connection.close()


def encrypt_and_store_aws_credentials(user_id, access_key_id, secret_access_key, region):
    connection = get_db_connection()
    try:
        with connection.cursor() as cursor:
            encrypted_access_key_id = cipher.encrypt(access_key_id.encode()).decode()
            encrypted_secret_access_key = cipher.encrypt(secret_access_key.encode()).decode()

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
    connection = get_db_connection()
    try:
        with connection.cursor() as cursor:
            cursor.execute(
                sql.SQL("SELECT access_key_id FROM aws_cloud_accounts WHERE user_id = %s"),
                (user_id,)
            )
            rows = cursor.fetchall()

            for row in rows:
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
    logging.info(f"Loaded and decoded SECRET_KEY: {SECRET_KEY}")  # For debugging; remove in production
    if not token:
        logging.error("No token provided")
        return None

    try:
        logging.info(f"Token received: {token}")

        # Decode the token using the decoded secret key and HS256 algorithm
        payload = jwt.decode(token.split(" ")[1], SECRET_KEY, algorithms=["HS256"])
        logging.info(f"Decoded payload: {payload}")
        return payload['sub']  # Adjust according to your token structure
    except jwt.ExpiredSignatureError:
        logging.error("Token has expired")
        return None
    except jwt.InvalidTokenError as e:
        logging.error(f"Invalid token: {e}")
        return None


def get_user_id_from_username(username):
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
            all_accounts = decrypted_aws_accounts + [{'cloud_name': row[0], 'project_id': row[1]} for row in gcp_accounts] \
                + [{'cloud_name': row[0], 'client_id': row[1]} for row in azure_accounts]

            return all_accounts

    except Exception as e:
        logging.error(f"Error fetching cloud accounts: {e}")
        return None
    finally:
        if connection:
            connection.close()
