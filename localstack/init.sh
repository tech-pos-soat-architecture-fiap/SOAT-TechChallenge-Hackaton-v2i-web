#!/usr/bin/env bash
set -euo pipefail

BUCKET_NAME="v2i-bucket"
LAMBDA_NAME="s3-video-event-handler"
REGION="us-east-1"
ACCOUNT_ID="000000000000"
LAMBDA_DIR="/lambdas"
LAMBDA_ZIP="${LAMBDA_DIR}/s3_event_handler.zip"

log() { echo "[localstack-ready] $*"; }

log "Installing requests and packaging Lambda zip"
pip install requests --target "${LAMBDA_DIR}/package" -q
cp "${LAMBDA_DIR}/s3_event_handler.py" "${LAMBDA_DIR}/package/s3_event_handler.py"
python3 -c "
import zipfile, os

package_dir = '${LAMBDA_DIR}/package'
zip_path = '${LAMBDA_ZIP}'

with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as z:
    for root, dirs, files in os.walk(package_dir):
        for file in files:
            abs_path = os.path.join(root, file)
            arcname = os.path.relpath(abs_path, package_dir)
            z.write(abs_path, arcname)

print('Zip created:', zip_path)
"

log "Creating S3 bucket: ${BUCKET_NAME}"
awslocal s3api create-bucket --bucket "${BUCKET_NAME}" --region "${REGION}" >/dev/null

log "Creating Lambda function: ${LAMBDA_NAME}"
awslocal lambda create-function \
  --function-name "${LAMBDA_NAME}" \
  --runtime python3.12 \
  --role "arn:aws:iam::${ACCOUNT_ID}:role/lambda-role" \
  --handler s3_event_handler.handler \
  --zip-file "fileb://${LAMBDA_ZIP}" \
  --region "${REGION}" \
  --timeout 30 >/dev/null

log "Waiting for Lambda to become active..."
awslocal lambda wait function-active --function-name "${LAMBDA_NAME}" --region "${REGION}"

log "Granting S3 permission to invoke Lambda"
# add-permission can fail if re-run; ignore if statement already exists
awslocal lambda add-permission \
  --function-name "${LAMBDA_NAME}" \
  --statement-id "s3-invoke-permission" \
  --action "lambda:InvokeFunction" \
  --principal s3.amazonaws.com \
  --source-arn "arn:aws:s3:::${BUCKET_NAME}" \
  --region "${REGION}" >/dev/null || true

log "Attaching S3 event notification -> Lambda"
awslocal s3api put-bucket-notification-configuration \
  --bucket "${BUCKET_NAME}" \
  --notification-configuration "{\"LambdaFunctionConfigurations\":[{\"LambdaFunctionArn\":\"arn:aws:lambda:${REGION}:${ACCOUNT_ID}:function:${LAMBDA_NAME}\",\"Events\":[\"s3:ObjectCreated:*\"],\"Filter\":{\"Key\":{\"FilterRules\":[{\"Name\":\"prefix\",\"Value\":\"uploads/\"}]}}}]}" >/dev/null

log "Setup complete!"

