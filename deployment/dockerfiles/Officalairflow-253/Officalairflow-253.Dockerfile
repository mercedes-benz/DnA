FROM registry-emea.app.corpintra.net/dnaplatform/airflow/officalairflow:2.5.3-u50000
USER root
ARG PROXY=""
ARG NO_PROXY=""
ENV http_proxy=${PROXY}
ENV https_proxy=${PROXY}
ENV HTTP_PROXY=${PROXY}
ENV HTTPS_PROXY=${PROXY}
ENV no_proxy=${NO_PROXY}
ENV NO_PROXY=${NO_PROXY}
ADD chain.crt /usr/local/share/ca-certificates/ca-bundle.crt
RUN chmod 644 /usr/local/share/ca-certificates/ca-bundle.crt && update-ca-certificates
USER 50000
COPY requirements.txt /
RUN pip install --no-cache-dir "apache-airflow==${AIRFLOW_VERSION}" -r /requirements.txt
ENV REQUESTS_CA_BUNDLE="/usr/local/share/ca-certificates/ca-bundle.crt"
ENV SSL_CERT_FILE="/usr/local/share/ca-certificates/ca-bundle.crt"