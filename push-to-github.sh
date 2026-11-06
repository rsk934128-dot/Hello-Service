#!/usr/bin/env bash
# NoorLink GitHub Deployment Script for rsk934128-dot

set -e

REPO_NAME="noorlink"
GITHUB_USER="rsk934128-dot"
REMOTE_URL="https://github.com/${GITHUB_USER}/${REPO_NAME}.git"

echo "=================================================="
echo "⚡ NoorLink GitHub Release & Push Tool"
echo "👤 Target GitHub: https://github.com/${GITHUB_USER}"
echo "📦 Repository: ${REMOTE_URL}"
echo "=================================================="

# Check if remote origin already exists
if git remote | grep -q 'origin'; then
    echo "Updating existing remote origin..."
    git remote set-url origin "$REMOTE_URL"
else
    echo "Adding remote origin..."
    git remote add origin "$REMOTE_URL"
fi

echo "Current Git Status:"
git status -s
echo "Current Branch: $(git branch --show-current)"
echo "Current Tag: $(git tag -l 'v*')"

echo ""
echo "To push this repository to your GitHub account, run:"
echo "--------------------------------------------------"
echo "git push -u origin main"
echo "git push origin v1.0.0"
echo "--------------------------------------------------"
