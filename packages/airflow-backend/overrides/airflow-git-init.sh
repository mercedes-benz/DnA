#!/bin/sh

echo "Trying to clone Airflow Dag Repo..."
cd /git
export GIT_ASKPASS=/tmp/git-askpass-helper.sh
git clone https://PID2BB1@git.daimler.com/DNA/airflow-user-dags.git | exit 0