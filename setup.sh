#!/usr/bin/env bash
set -euo pipefail

echo "[setup.sh] 1. Mise à jour du cache APT…"
apt-get update -y

echo "[setup.sh] 2. Installation de Maven (apache-maven)…"
apt-get install -y maven

echo "[setup.sh] 3. Vérification de l’installation :"
mvn --version
