#!/usr/bin/env bash
# Prints the GitHub release notes for one version: its CHANGELOG.md section plus a download table.
# Usage: scripts/release-notes.sh 1.0.0
set -euo pipefail
cd "$(dirname "$0")/.."
version="${1:?usage: release-notes.sh <version>}"

notes="$(awk -v v="$version" '
  index($0, "## [" v "]") == 1 { found = 1; next }
  found && /^## \[/ { exit }
  found { print }
' CHANGELOG.md)"
if [ -z "$(printf '%s' "$notes" | tr -d '[:space:]')" ]; then
  echo "No section for version $version in CHANGELOG.md" >&2
  exit 1
fi

printf '%s\n\n' "$notes"
cat <<NOTES
### Which file do I need?

| Minecraft | Fabric | NeoForge | Forge |
|---|---|---|---|
| 26.3   | \`villagecensus-fabric-${version}+mc26.3.jar\` | \`villagecensus-neoforge-${version}+mc26.3.jar\` | \`villagecensus-forge-${version}+mc26.3.jar\` |
| 26.2   | \`villagecensus-fabric-${version}+mc26.2.jar\` | \`villagecensus-neoforge-${version}+mc26.2.jar\` | \`villagecensus-forge-${version}+mc26.2.jar\` |
| 26.1.2 | \`villagecensus-fabric-${version}+mc26.1.2.jar\` | \`villagecensus-neoforge-${version}+mc26.1.2.jar\` | \`villagecensus-forge-${version}+mc26.1.2.jar\` |

* Install the mod on both the client and the server.
* **Fabric** also needs [Fabric API](https://modrinth.com/mod/fabric-api).
* Java 25 is required (the same as Minecraft 26.x itself).
NOTES
