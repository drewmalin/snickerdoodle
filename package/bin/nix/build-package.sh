#!/bin/bash

set -e

# Type of artifact.
# E.g.:
#  - exe
#  - deb
#  - dmg
INSTALLER_TYPE=${1}

# Input path.
# E.g.:
#  - path/to/jars/
INPUT=${2}

# Entrypoint jar.
# E.g.:
#  - foo.jar
MAIN_JAR=${3}

# Entrypoint class (fully-qualified).
# E.g.:
#  - com.foo.bar.Main
MAIN_CLASS=${4}

# Application name.
APP_NAME=${5}

# Application version.
APP_VERSION=${6}

# Application icon (filepath).
# E.g.:
#  - myicon.png (*nix, mac)
#  - myicon.ico (windows)
APP_ICON=${7}

# Output path.
# E.g.:
#  - path/to/output/
OUTPUT=${8}

jpackage \
  --type "${INSTALLER_TYPE}" \
  --input "${INPUT}" \
  --main-jar "${MAIN_JAR}" \
  --main-class "${MAIN_CLASS}" \
  --name "${APP_NAME}" \
  --app-version "${APP_VERSION}" \
  --icon "${APP_ICON}" \
  --dest "${OUTPUT}" \
  --verbose