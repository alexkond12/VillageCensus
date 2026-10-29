#!/usr/bin/env bash
# One-time setup of the Git repository (see setup-git.bat for details). Nothing is pushed.
set -euo pipefail
cd "$(dirname "$0")/.."
[ -d .git ] && { echo "Already a Git repository."; exit 0; }
mkdir -p .github/workflows
cp ci/github-workflow-build.yml .github/workflows/build.yml
git init -b main
git config user.name >/dev/null || git config user.name "alexkond12"
git config user.email >/dev/null || git config user.email "alexkond12@users.noreply.github.com"
git add -A
git commit -m "Initial commit: Village Census 1.0.0"
echo "Done. Repository created with one commit on branch \"main\"."
