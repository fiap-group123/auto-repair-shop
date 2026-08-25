#!/bin/sh
set -e

cd "$(git rev-parse --show-toplevel)" || exit 1

mkdir -p .git/hooks
cp hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
