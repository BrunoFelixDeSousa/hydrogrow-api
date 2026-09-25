#!/usr/bin/env sh

set -e

git config core.hooksPath .githooks
chmod +x .githooks/pre-commit
chmod +x .githooks/pre-push

echo "Git hooks configurados."