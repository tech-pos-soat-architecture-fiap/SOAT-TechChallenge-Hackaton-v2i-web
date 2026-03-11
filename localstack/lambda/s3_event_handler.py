import json
import os
import requests

PROCESSING_APP_URL = os.environ.get("PROCESSING_APP_URL", "http://v2i-processing:8080/api/video/process")


def handler(event, context):
    print("S3 event received:", json.dumps(event))

    for record in event.get("Records", []):
        bucket = record["s3"]["bucket"]["name"]
        key = record["s3"]["object"]["key"]

        print(f"New object uploaded -> bucket: {bucket}, key: {key}")

        _send_to_processing_app(bucket, key)

    return {"statusCode": 200, "body": "OK"}


def _send_to_processing_app(bucket: str, key: str):
    payload = {"bucket": bucket, "key": key}

    try:
        response = requests.post(PROCESSING_APP_URL, json=payload, timeout=10)
        response.raise_for_status()
        print(f"Processing app responded [{response.status_code}]: {response.text}")
    except requests.HTTPError as e:
        print(f"HTTP error calling processing app: {e.response.status_code} {e.response.reason}")
        raise
    except requests.ConnectionError as e:
        print(f"Failed to reach processing app: {e}")
        raise
