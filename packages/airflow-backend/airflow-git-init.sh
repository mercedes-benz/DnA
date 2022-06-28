#!/bin/sh

echo "Trying to clone Airflow Dag Repo..."
cd /git
export GIT_ASKPASS=/tmp/git-askpass-helper.sh
git clone $AIRFLOW_GIT_URI | exit 0

