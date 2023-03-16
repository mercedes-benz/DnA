FROM debian:stretch-slim

#ARG PROXY=""

#ENV http_proxy=${PROXY}
#ENV https_proxy=${PROXY}

# Debian Base to use
ENV DEBIAN_VERSION stretch

# initial install of av daemon
RUN echo "deb http://http.debian.net/debian/ $DEBIAN_VERSION main contrib non-free" > /etc/apt/sources.list && \
    echo "deb http://http.debian.net/debian/ $DEBIAN_VERSION-updates main contrib non-free" >> /etc/apt/sources.list && \
    echo "deb http://security.debian.org/ $DEBIAN_VERSION/updates main contrib non-free" >> /etc/apt/sources.list && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install --no-install-recommends -y -qq \
    clamav-daemon \
    clamav-freshclam \
    libclamunrar9 \
    wget && \
    apt-get clean && \
    apt-get install ca-certificates openssl && \
    update-ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# initial update of av databases
RUN wget --user-agent='CVDUPDATE/0' -O /var/lib/clamav/main.cvd https://database.clamav.net/main.cvd 
RUN wget --user-agent='CVDUPDATE/0' -O /var/lib/clamav/daily.cvd https://database.clamav.net/daily.cvd
RUN wget --user-agent='CVDUPDATE/0' -O /var/lib/clamav/bytecode.cvd https://database.clamav.net/bytecode.cvd
RUN chown clamav:clamav /var/lib/clamav/*.cvd

# Local copy - for testing
#COPY --chown=clamav:clamav ./db/*.cvd /var/lib/clamav/

# Copy from runner
# COPY --chown=clamav:clamav ./runner-db/*.cvd /var/lib/clamav/
# COPY --chown=clamav:clamav ./runner-db/daily.cld /var/lib/clamav/

# av configuration update
RUN sed -i 's/^Foreground .*$/Foreground true/g' /etc/clamav/clamd.conf && \
    echo "TCPSocket 3310" >> /etc/clamav/clamd.conf && \
    # if ! [ -z $HTTPProxyServer ]; then echo "HTTPProxyServer $HTTPProxyServer" >> /etc/clamav/freshclam.conf; fi && \
    # if ! [ -z $HTTPProxyPort   ]; then echo "HTTPProxyPort $HTTPProxyPort" >> /etc/clamav/freshclam.conf; fi && \
    sed -i 's/^Foreground .*$/Foreground true/g' /etc/clamav/freshclam.conf

# permission juggling
RUN mkdir /var/run/clamav && \
    chown clamav:clamav /var/run/clamav && \
    chmod 750 /var/run/clamav && \
    chown -R clamav:clamav /var/log/clamav/

RUN useradd clamav_user -G clamav -u 1000 -s /var/lib/clamav && \
    chown -R clamav_user:clamav /var/lib/clamav /etc/clamav /var/run/clamav

# port provision
EXPOSE 3310

COPY ./freshclam.conf /usr/local/etc/freshclam.conf
COPY ./clamd.conf /etc/clamav/clamd.conf

COPY ./bootstrap.sh /
COPY ./check.sh /

RUN chown clamav_user:clamav /etc/ssl/certs

RUN chown clamav_user:clamav bootstrap.sh check.sh /etc/clamav /etc/clamav/clamd.conf /etc/clamav/freshclam.conf /var/log/clamav/clamav.log /var/log/clamav/freshclam.log && \
    chmod u+x bootstrap.sh check.sh

USER 1000

CMD ["/bootstrap.sh"]

# Use multistage build in order to unset proxy
#FROM builder

#ENV http_proxy=""
#ENV https_proxy=""
