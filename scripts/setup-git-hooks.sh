#!/usr/bin/env sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

log_step() {
    printf "${BLUE}➜ %s${NC}\n" "$1"
}

log_step "Configurando Git hooks..."

git config core.hooksPath .githooks
chmod +x .githooks/pre-commit
chmod +x .githooks/pre-push

log_step "Git hooks configurados."