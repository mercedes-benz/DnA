FROM codercom/code-server:4.10.1
COPY proxy.conf /etc/apt/apt.conf.d/proxy.conf
USER root

    
RUN sudo apt-get update \
 && sudo apt-get install -y \
 openjdk-17-jre \
 openjdk-17-jdk \
 unzip \
 wget \
 python3-pip 

## Install Nodejs 
RUN sudo curl -fsSL https://deb.nodesource.com/setup_18.x | bash - &&\
    sudo apt-get install -y nodejs

RUN sudo apt-get update
RUN sudo apt-get install nodejs

RUN npm install --global yarn
RUN npm install -g @angular/cli

RUN sudo mkdir /opt/gradle
WORKDIR /opt/gradle
# RUN sudo wget https://downloads.gradle-dn.com/distributions/gradle-7.2-bin.zip
COPY gradle-7.2-bin.zip .
RUN sudo unzip gradle-7.2-bin.zip


#Install Minio Client
WORKDIR /usr/local/bin/
# RUN sudo wget https://dl.min.io/client/mc/release/linux-amd64/mc
COPY ./mc .
RUN sudo chmod +x mc

## Add extra  Envirnoment variable for code server
COPY ./globalenv.sh /etc/profile.d/
RUN sudo chmod +x /etc/profile.d/globalenv.sh

USER 1000
WORKDIR /home/coder
RUN chown -R 1000:1000 /home/coder
