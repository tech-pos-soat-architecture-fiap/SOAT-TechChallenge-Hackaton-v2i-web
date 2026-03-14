#!/usr/bin/env bash
set -euo pipefail

BUCKET_NAME="v2i-bucket"
REGION="us-east-1"

log() { echo "[localstack-ready] $*"; }

log "Creating S3 bucket: ${BUCKET_NAME}"
awslocal s3api create-bucket --bucket "${BUCKET_NAME}" --region "${REGION}" >/dev/null || true

log "Setup complete! (S3 only)"
