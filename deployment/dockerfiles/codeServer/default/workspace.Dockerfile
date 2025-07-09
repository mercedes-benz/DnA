# Author: Sathishkumar - PALANSA
FROM registry-emea.app.corpintra.net/dockerhub/codercom/code-server:4.99.3

COPY proxy.conf /etc/apt/apt.conf.d/proxy.conf

USER root

# Add CA bundle to main ca-certificates folder
ADD Corp-Root-CA-G2.crt /usr/local/share/ca-certificates/ca-bundle.crt
RUN chmod 644 /usr/local/share/ca-certificates/ca-bundle.crt && update-ca-certificates

RUN sudo apt-get update \
 && sudo apt-get install -y \
    unzip \
    zip \
    wget \
    jq \
    net-tools \
    postgresql-client \
    libncursesw5-dev \
    libssl-dev \
    libsqlite3-dev \
    tk-dev \
    libgdbm-dev \
    libc6-dev \
    libbz2-dev \
    libffi-dev \
    zlib1g-dev \
    build-essential \
    libreadline-dev \
    make \
    llvm \
    libncurses5-dev \
    xz-utils \
    liblzma-dev \
    python3-openssl \
    iputils-ping \
    telnet \
    netcat-traditional \
    dnsutils \
    traceroute \
    tcpdump \
    libglib2.0-0 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libxkbcommon0 \
    libpango-1.0-0 \
    libcairo2 \
    libasound2 \
    libatspi2.0-0 \
    portaudio19-dev \
    python3-pyaudio \
    poppler-utils \
    libreoffice

# Manually download and add the Microsoft repository key
RUN curl -fsSL https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor -o /usr/share/keyrings/microsoft-prod.gpg

# Add Microsoft's repository
RUN curl https://packages.microsoft.com/config/debian/$(lsb_release -rs)/prod.list | tee /etc/apt/sources.list.d/mssql-release.list

# Update and install the ODBC Driver for SQL Server
RUN apt-get update && ACCEPT_EULA=Y apt-get install -y msodbcsql18 mssql-tools18 unixodbc-dev \
    && apt-get clean

# Install Minio Client
WORKDIR /usr/local/bin/
RUN sudo wget https://dl.min.io/client/mc/release/linux-amd64/mc
RUN sudo chmod +x mc

USER 1000
WORKDIR /home/coder
RUN chown -R 1000:1000 /home/coder