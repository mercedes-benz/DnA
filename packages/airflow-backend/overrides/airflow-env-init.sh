#!/usr/bin/env bash

set -x

cd /usr/local/lib/python3.7/site-packages/airflow && \

airflow initdb && \
(airflow create_user --username kamesh --lastname Rao --firstname Kamesh --email kameshwara.rao@daimler.com --role Admin --password airflow || true) 
# && \
# (airflow create_user --username xiaoyz --lastname Zhang --firstname Xiaoyu --email xiaoyu.z.zhang@daimler.com --role Admin --password airflow || true)
