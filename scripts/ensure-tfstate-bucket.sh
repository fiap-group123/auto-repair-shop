#!/usr/bin/env bash
set -euo pipefail

bucket="${1:-fiap-group123-auto-repair-shop-tfstate}"
region="${AWS_REGION:-us-east-1}"

if aws s3api head-bucket --bucket "$bucket" 2>/dev/null; then
  echo "Bucket ${bucket} already exists"
else
  aws s3api create-bucket --bucket "$bucket" --region "$region"
fi

aws s3api put-bucket-versioning \
  --bucket "$bucket" \
  --versioning-configuration Status=Enabled

aws s3api put-bucket-encryption \
  --bucket "$bucket" \
  --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

aws s3api put-public-access-block \
  --bucket "$bucket" \
  --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
