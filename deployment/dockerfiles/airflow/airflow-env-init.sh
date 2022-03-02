#!/usr/bin/env bash

set -x

cd /usr/local/lib/python3.7/site-packages/airflow && \

airflow initdb && \
(airflow create_user --username dna --lastname user --firstname dna --email user@dna.com --role Admin --password xxxx || true) 
