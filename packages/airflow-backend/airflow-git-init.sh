#!/bin/sh

echo "Trying to clone Airflow Dag Repo..."
cd /git
export GIT_ASKPASS=/tmp/git-askpass-helper.sh
git clone https://vardhandevalla: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@github.com/Vardhandevalla/airflow_dags.git | exit 0

