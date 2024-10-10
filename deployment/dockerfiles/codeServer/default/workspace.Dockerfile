# Author : Sathishkumar
FROM registry-emea.app.corpintra.net/dockerhub/codercom/code-server:4.92.2
COPY proxy.conf /etc/apt/apt.conf.d/proxy.conf
USER root

# add ca_bundle to main ca-certificates folder
ADD Corp-Root-CA-G2.crt /usr/local/share/ca-certificates/ca-bundle.crt
RUN chmod 644 /usr/local/share/ca-certificates/ca-bundle.crt && sudo update-ca-certificates
    
RUN sudo apt-get update \
 && sudo apt-get install -y \
 unzip \
 zip \
 wget \
 jq \
 net-tools \
 postgresql-client \
 libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev libffi-dev zlib1g-dev wget build-essential libreadline-dev \
 make llvm libncurses5-dev xz-utils liblzma-dev python3-openssl iputils-ping telnet netcat-traditional dnsutils traceroute tcpdump

#Install Minio Client
WORKDIR /usr/local/bin/
RUN sudo wget https://dl.min.io/client/mc/release/linux-amd64/mc
RUN sudo chmod +x mc

USER 1000
WORKDIR /home/coder
RUN chown -R 1000:1000 /home/coder
