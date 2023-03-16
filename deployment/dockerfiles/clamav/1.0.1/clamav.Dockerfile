###############################################################################
# Author: Sathishkumar Palani
# Created a docker file to fix the clamav vulnerability 
# https://thehackernews.com/2023/02/critical-rce-vulnerability-discovered.html
################################################################################
FROM clamav/clamav:latest
COPY ./clamd.conf /etc/clamav/
COPY ./freshclam.conf /etc/clamav/
RUN wget --user-agent='CVDUPDATE/0' -O /var/lib/clamav/main.cvd https://database.clamav.net/main.cvd 
RUN wget --user-agent='CVDUPDATE/0' -O /var/lib/clamav/daily.cvd https://database.clamav.net/daily.cvd
RUN wget --user-agent='CVDUPDATE/0' -O /var/lib/clamav/bytecode.cvd https://database.clamav.net/bytecode.cvd
USER root
RUN chown clamav:clamav -R /var/lib/clamav
RUN chown clamav:clamav -R /var/lock/
RUN chown clamav:clamav -R /etc/clamav
RUN chown clamav:clamav -R /etc/clamav
RUN chown clamav:clamav -R /run
USER 100