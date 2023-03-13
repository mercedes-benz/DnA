FROM codercom/code-server:4.10.1
COPY proxy.conf /etc/apt/apt.conf.d/proxy.conf
RUN sudo apt-get update \
 && sudo apt-get install -y \
 openjdk-17-jre \
 openjdk-17-jdk \
 nodejs \
 npm \ 
 unzip \
 wget \
 python3-pip 
# RUN sudo mkdir /opt/gradle
# WORKDIR /opt/gradle
# RUN sudo wget https://downloads.gradle-dn.com/distributions/gradle-7.2-bin.zip
# RUN sudo unzip gradle-7.2-bin.zip
# RUN echo "export PATH=/opt/gradle/gradle-7.2/bin:${PATH}" | sudo tee /etc/profile.d/gradle.sh
# RUN sudo chmod +x /etc/profile.d/gradle.sh
WORKDIR /home/coder
RUN chown -R 1000:1000 /home/coder