import json
import os
import requests

# When running under LocalStack in Docker, the Lambda runs in an isolated container.
# This reads PROCESSING_APP_URL from environment, with fallback to host.docker.internal:18000.
# To use, ensure kubectl port-forward runs with --address 0.0.0.0:
#   kubectl port-forward -n default svc/v2i-processing-service-v1 18000:80 --address 0.0.0.0
PROCESSING_APP_URL = os.environ.get(
    "PROCESSING_APP_URL",
    "http://host.docker.internal:18000/extract-frames",
)

# LocalStack S3 endpoint used to build a URL that the *processing pod* can reach.
# IMPORTANT: do NOT use a Docker-internal IP (172.x/172.22.x) here.
S3_ENDPOINT = os.environ.get(
    "S3_ENDPOINT",
    os.environ.get("AWS_ENDPOINT_URL", "http://localhost:4566"),
)


def handler(event, context):
    print("S3 event received:", json.dumps(event))

    for record in event.get("Records", []):
        bucket = record["s3"]["bucket"]["name"]
        key = record["s3"]["object"]["key"]

        print(f"New object uploaded -> bucket: {bucket}, key: {key}")

        _send_to_processing_app(bucket, key)

    return {"statusCode": 200, "body": "OK"}


def _send_to_processing_app(bucket: str, key: str):
    file_url = f"{S3_ENDPOINT.rstrip('/')}/{bucket}/{key}"
    payload = {"bucket": bucket, "key": key, "url": file_url}

    try:
        # Helpful one-liner for debugging connectivity.
        print(f"Calling processing app -> url={PROCESSING_APP_URL} payload={payload}")

        # Use (connect, read) timeouts to avoid hanging Lambdas.
        response = requests.post(PROCESSING_APP_URL, json=payload, timeout=(5, 30))
        response.raise_for_status()
        print(f"Processing app responded [{response.status_code}]: {response.text}")
    except requests.Timeout as e:
        print(f"Timeout calling processing app: {e}")
        raise
    except requests.HTTPError as e:
        print(f"HTTP error calling processing app: {e.response.status_code} {e.response.reason} body={getattr(e.response, 'text', '')}")
        raise
    except requests.ConnectionError as e:
        print(f"Failed to reach processing app: {e}")
        raise
