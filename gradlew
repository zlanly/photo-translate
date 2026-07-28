#!/bin/sh

# This script is a Gradle wrapper that downloads and runs Gradle
# from the distribution specified in gradle-wrapper.properties.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_PROPERTIES="$SCRIPT_DIR/gradle-wrapper.properties"

if [ ! -f "$WRAPPER_PROPERTIES" ]; then
  echo "Error: gradle-wrapper.properties not found"
  exit 1
fi

# Parse distributionUrl
DIST_URL=""
while IFS= read -r line; do
  case "$line" in
    distributionUrl=*)
      DIST_URL=$(echo "$line" | sed 's/^.*=//;s/\\//g')
      ;;
  esac
done < "$WRAPPER_PROPERTIES"

if [ -z "$DIST_URL" ]; then
  echo "Error: Could not find distributionUrl in gradle-wrapper.properties"
  exit 1
fi

# Extract version from URL for directory naming
VERSION=$(echo "$DIST_URL" | grep -oP 'gradl?e-[0-9.]+')
VERSION=${VERSION#gradle-}
VERSION=${VERSION#gradle-}

DIST_DIR="${HOME:-/root}/.gradle/wrapper/dists/$VERSION"

# Check if Gradle is already extracted
if [ -d "$DIST_DIR/gradle-$VERSION" ] && [ -x "$DIST_DIR/gradle-$VERSION/bin/gradle" ]; then
  exec "$DIST_DIR/gradle-$VERSION/bin/gradle" "$@"
fi

# Download directory
DOWNLOAD_DIR="$DIST_DIR/.unpacked"
mkdir -p "$DOWNLOAD_DIR"
ZIP_FILE="$DOWNLOAD_DIR/gradle-$VERSION.zip"

echo "Downloading Gradle $VERSION from $DIST_URL..."

# Use curl if available, otherwise wget
if command -v curl &> /dev/null; then
  curl -L --progress-bar -o "$ZIP_FILE" "$DIST_URL"
elif command -v wget &> /dev/null; then
  wget -O "$ZIP_FILE" "$DIST_URL"
else
  echo "Error: Neither curl nor wget is available."
  exit 1
fi

if [ ! -s "$ZIP_FILE" ]; then
  echo "Error: Download failed or file is empty"
  rm -f "$ZIP_FILE"
  exit 1
fi

echo "Extracting Gradle..."
unzip -q "$ZIP_FILE" -d "$DIST_DIR"

# Clean up
rm -f "$ZIP_FILE"
rmdir "$DOWNLOAD_DIR" 2>/dev/null || true

# Run gradle from extracted directory
EXECUTABLE="$DIST_DIR/gradle-$VERSION/bin/gradle"
if [ -x "$EXECUTABLE" ]; then
  exec "$EXECUTABLE" "$@"
else
  echo "Error: Gradle executable not found at $EXECUTABLE"
  echo "Please check the download and permissions."
  exit 1
fi
