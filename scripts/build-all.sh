#!/usr/bin/env bash
# ==================================================================================
#  Builds Village Census for every supported Minecraft version and every loader.
#  The finished jars are collected in the dist/ folder.
#  Usage: ./scripts/build-all.sh
# ==================================================================================
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew cleanDist
for v in 26.1.2 26.2 26.3; do
    echo
    echo "===== Building for Minecraft $v ====="
    ./gradlew build -Pmc_target="$v"
done
echo
echo "All jars are in $(pwd)/dist:"
ls -1 dist
