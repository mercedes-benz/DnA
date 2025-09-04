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
# Install OS dependencies


# RUN apt-get update && apt-get install -y \
#     libaio1 \
#     unzip \
#     telnet \
#     traceroute \
#     && rm -rf /var/lib/apt/lists/*

RUN rm -rf /etc/apt/sources.list.d/mysql.list
RUN apt-get update && apt-get install -y unzip wget

RUN mkdir -p /opt/oracle
# Set Oracle environment variables
ENV ORACLE_BASE=/opt/oracle
ENV LD_LIBRARY_PATH=$ORACLE_BASE/instantclient
ENV PATH=$LD_LIBRARY_PATH:$PATH

# Copy all downloaded Oracle Instant Client ZIP files (ARM64)
RUN wget https://download.oracle.com/otn_software/linux/instantclient/2380000/instantclient-basic-linux.x64-23.8.0.25.04.zip -P /opt/oracle/
RUN wget https://download.oracle.com/otn_software/linux/instantclient/2380000/instantclient-sdk-linux.x64-23.8.0.25.04.zip -P /opt/oracle/


# Unzip all Oracle Instant Client packages
RUN cd /opt/oracle && \
    unzip -o /opt/oracle/instantclient-sdk-linux.x64-23.8.0.25.04.zip  && \
    unzip -o /opt/oracle/instantclient-basic-linux.x64-23.8.0.25.04.zip  && \
    ln -s /opt/oracle/instantclient_23_8 /opt/oracle/instantclient && \
    rm *.zip

# Fix permissions for the airflow user
RUN chown -R airflow: /opt/oracle

ADD chain.crt /usr/local/share/ca-certificates/ca-bundle.crt
RUN chmod 644 /usr/local/share/ca-certificates/ca-bundle.crt && update-ca-certificates
USER 50000
COPY requirements.txt /
RUN pip install --no-cache-dir "apache-airflow==${AIRFLOW_VERSION}" -r /requirements.txt
ENV REQUESTS_CA_BUNDLE="/usr/local/share/ca-certificates/ca-bundle.crt"
ENV SSL_CERT_FILE="/usr/local/share/ca-certificates/ca-bundle.crt"